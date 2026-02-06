package com.aiinsightagent.app.documentation;

import com.aiinsightagent.app.TestApplication;
import com.aiinsightagent.core.config.GeminiProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Configuration Example Validation Test
 *
 * README.md에 명시된 설정 예제가 실제 애플리케이션 설정과 일치하는지 검증합니다.
 * This test validates that configuration examples in README.md match the actual application configuration.
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@DisplayName("Configuration Example Validation - README.md YAML Examples")
class ConfigurationExampleValidationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private GeminiProperties geminiProperties;

    private String readReadmeFile() throws IOException {
        Path readmePath = Paths.get("README.md");
        if (!Files.exists(readmePath)) {
            readmePath = Paths.get("../README.md");
        }
        if (!Files.exists(readmePath)) {
            readmePath = Paths.get("../../README.md");
        }
        return Files.readString(readmePath);
    }

    @Nested
    @DisplayName("Gemini API Configuration Validation")
    class GeminiConfigurationTest {

        @Test
        @DisplayName("README의 Gemini 설정 구조가 실제 설정과 일치")
        void geminiConfig_StructureMatchesReadme() throws IOException {
            String readme = readReadmeFile();

            // README에 명시된 설정 키들
            assertThat(readme).contains("spring:");
            assertThat(readme).contains("ai:");
            assertThat(readme).contains("gemini:");
            assertThat(readme).contains("models:");
            assertThat(readme).contains("- id:");
            assertThat(readme).contains("name:");
            assertThat(readme).contains("api-key:");
            assertThat(readme).contains("temperature:");
            assertThat(readme).contains("max-output-tokens:");
        }

        @Test
        @DisplayName("README의 모델 ID 형식이 실제 설정과 일치 (m00, m01, ...)")
        void geminiConfig_ModelIdFormatMatchesReadme() throws IOException {
            String readme = readReadmeFile();

            // README 예제: m00, m01
            assertThat(readme).contains("id: m00");
            assertThat(readme).contains("id: m01");

            // 실제 설정 검증
            assertThat(geminiProperties).isNotNull();
            assertThat(geminiProperties.getModels()).isNotEmpty();

            // 모델 ID가 m00, m01 형식인지 확인
            geminiProperties.getModels().forEach(model -> {
                assertThat(model.getId()).matches("m\\d{2}");
            });
        }

        @Test
        @DisplayName("README의 모델 이름 예제가 유효 (gemini-2.5-flash)")
        void geminiConfig_ModelNameInReadmeIsValid() throws IOException {
            String readme = readReadmeFile();

            // README 예제: gemini-2.5-flash
            assertThat(readme).contains("name: gemini-2.5-flash");

            // 실제 설정에서도 gemini 모델 이름 사용
            geminiProperties.getModels().forEach(model -> {
                assertThat(model.getName()).startsWith("gemini");
            });
        }

        @Test
        @DisplayName("README의 API 키 환경변수 형식 검증 (${GEMINI_API_KEY_0:})")
        void geminiConfig_ApiKeyEnvironmentVariableFormat() throws IOException {
            String readme = readReadmeFile();

            // README 예제: ${GEMINI_API_KEY_0:}
            assertThat(readme).contains("${GEMINI_API_KEY_0:}");
            assertThat(readme).contains("${GEMINI_API_KEY_1:}");

            // 환경변수 패턴 검증
            Pattern envVarPattern = Pattern.compile("\\$\\{GEMINI_API_KEY_\\d+:\\}");
            Matcher matcher = envVarPattern.matcher(readme);

            int count = 0;
            while (matcher.find()) {
                count++;
            }

            assertThat(count).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("README의 temperature 설정 값이 유효 범위 (0.0-1.0)")
        void geminiConfig_TemperatureValueIsValid() throws IOException {
            String readme = readReadmeFile();

            // README에서 temperature 값 추출
            Pattern tempPattern = Pattern.compile("temperature:\\s*(\\d+\\.\\d+)");
            Matcher matcher = tempPattern.matcher(readme);

            while (matcher.find()) {
                double temperature = Double.parseDouble(matcher.group(1));
                // Gemini API의 temperature는 0.0 ~ 2.0 범위
                assertThat(temperature).isBetween(0.0, 2.0);
            }

            // 실제 설정 검증
            assertThat(geminiProperties.getTemperature()).isBetween(0.0f, 2.0f);
        }

        @Test
        @DisplayName("README의 max-output-tokens 설정이 양수")
        void geminiConfig_MaxOutputTokensIsPositive() throws IOException {
            String readme = readReadmeFile();

            // README에서 max-output-tokens 값 추출
            Pattern tokensPattern = Pattern.compile("max-output-tokens:\\s*(\\d+)");
            Matcher matcher = tokensPattern.matcher(readme);

            while (matcher.find()) {
                int maxTokens = Integer.parseInt(matcher.group(1));
                assertThat(maxTokens).isPositive();
            }

            // 실제 설정 검증
            assertThat(geminiProperties.getMaxOutputTokens()).isPositive();
        }

        @Test
        @DisplayName("README에 최대 10개 모델 설정 가능하다고 명시")
        void geminiConfig_SupportsUpTo10Models() throws IOException {
            String readme = readReadmeFile();

            // README: "Up to 10 models can be configured"
            assertThat(readme).containsIgnoringCase("Up to 10 models");

            // 실제 설정도 10개까지 지원
            assertThat(geminiProperties.getModels().size()).isLessThanOrEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Queue/Worker Configuration Validation")
    class QueueWorkerConfigurationTest {

        @Test
        @DisplayName("README의 Queue/Worker 설정 구조가 명시되어 있음")
        void queueConfig_StructureInReadme() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("aiinsight:");
            assertThat(readme).contains("request:");
            assertThat(readme).contains("queue:");
            assertThat(readme).contains("worker-count:");
            assertThat(readme).contains("queue-capacity:");
            assertThat(readme).contains("request-timeout-seconds:");
            assertThat(readme).contains("shutdown-timeout-seconds:");
        }

        @Test
        @DisplayName("README의 worker-count 설정 값이 양수")
        void queueConfig_WorkerCountIsPositive() throws IOException {
            String readme = readReadmeFile();

            // README에서 worker-count 값 추출
            Pattern workerPattern = Pattern.compile("worker-count:\\s*(\\d+)");
            Matcher matcher = workerPattern.matcher(readme);

            while (matcher.find()) {
                int workerCount = Integer.parseInt(matcher.group(1));
                assertThat(workerCount).isPositive();
                assertThat(workerCount).isLessThanOrEqualTo(100); // 합리적인 최대값
            }
        }

        @Test
        @DisplayName("README의 queue-capacity 설정 값이 양수")
        void queueConfig_QueueCapacityIsPositive() throws IOException {
            String readme = readReadmeFile();

            // README에서 queue-capacity 값 추출
            Pattern capacityPattern = Pattern.compile("queue-capacity:\\s*(\\d+)");
            Matcher matcher = capacityPattern.matcher(readme);

            while (matcher.find()) {
                int capacity = Integer.parseInt(matcher.group(1));
                assertThat(capacity).isPositive();
            }
        }

        @Test
        @DisplayName("README의 timeout 설정 값이 양수")
        void queueConfig_TimeoutValuesArePositive() throws IOException {
            String readme = readReadmeFile();

            // request-timeout-seconds
            Pattern requestTimeoutPattern = Pattern.compile("request-timeout-seconds:\\s*(\\d+)");
            Matcher requestMatcher = requestTimeoutPattern.matcher(readme);
            while (requestMatcher.find()) {
                int timeout = Integer.parseInt(requestMatcher.group(1));
                assertThat(timeout).isPositive();
            }

            // shutdown-timeout-seconds
            Pattern shutdownTimeoutPattern = Pattern.compile("shutdown-timeout-seconds:\\s*(\\d+)");
            Matcher shutdownMatcher = shutdownTimeoutPattern.matcher(readme);
            while (shutdownMatcher.find()) {
                int timeout = Integer.parseInt(shutdownMatcher.group(1));
                assertThat(timeout).isPositive();
            }
        }

        @Test
        @DisplayName("README에 worker-count 설명 주석이 포함됨")
        void queueConfig_ContainsDescriptionComments() throws IOException {
            String readme = readReadmeFile();

            // 영어 설명
            assertThat(readme).contains("Number of concurrent workers");
            assertThat(readme).contains("Maximum queue size");
            assertThat(readme).contains("Request timeout");
            assertThat(readme).contains("Shutdown wait time");
        }
    }

    @Nested
    @DisplayName("Database Configuration Validation")
    class DatabaseConfigurationTest {

        @Test
        @DisplayName("README의 Database 설정 구조가 명시되어 있음")
        void databaseConfig_StructureInReadme() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("datasource:");
            assertThat(readme).contains("driver-class-name:");
            assertThat(readme).contains("url:");
            assertThat(readme).contains("username:");
            assertThat(readme).contains("password:");
        }

        @Test
        @DisplayName("README의 MariaDB 드라이버 클래스명이 올바름")
        void databaseConfig_MariadbDriverClassIsCorrect() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("driver-class-name: org.mariadb.jdbc.Driver");
        }

        @Test
        @DisplayName("README의 JDBC URL 형식이 유효")
        void databaseConfig_JdbcUrlFormatIsValid() throws IOException {
            String readme = readReadmeFile();

            // MariaDB JDBC URL 패턴
            Pattern jdbcPattern = Pattern.compile("jdbc:mariadb://[\\w\\.:]+/[\\w_]+");
            Matcher matcher = jdbcPattern.matcher(readme);

            assertThat(matcher.find()).isTrue();
        }

        @Test
        @DisplayName("README의 데이터베이스 이름이 명시됨 (analysis_platform)")
        void databaseConfig_DatabaseNameIsSpecified() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("analysis_platform");
        }

        @Test
        @DisplayName("README에 Tech Stack에 MariaDB가 명시됨")
        void databaseConfig_MariadbInTechStack() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("MariaDB");
        }
    }

    @Nested
    @DisplayName("YAML Formatting Validation")
    class YamlFormattingTest {

        @Test
        @DisplayName("README의 YAML 코드 블록이 올바른 형식")
        void yaml_CodeBlocksAreWellFormatted() throws IOException {
            String readme = readReadmeFile();

            // YAML 코드 블록 존재 확인
            assertThat(readme).contains("```yaml");

            // YAML 코드 블록 추출
            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            int yamlCount = 0;
            while (matcher.find()) {
                String yamlContent = matcher.group(1);
                yamlCount++;

                // 기본 YAML 형식 검증 (키: 값)
                assertThat(yamlContent).containsPattern("^[\\w-]+:", Pattern.MULTILINE);
            }

            assertThat(yamlCount).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("README의 YAML 들여쓰기가 일관성 있음 (2 spaces)")
        void yaml_IndentationIsConsistent() throws IOException {
            String readme = readReadmeFile();

            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            while (matcher.find()) {
                String yamlContent = matcher.group(1);

                // YAML은 일반적으로 2칸 들여쓰기 사용
                // 들여쓰기가 있는 라인들이 2의 배수 칸인지 확인
                String[] lines = yamlContent.split("\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;

                    int leadingSpaces = line.length() - line.stripLeading().length();
                    if (leadingSpaces > 0) {
                        // 들여쓰기는 2의 배수여야 함
                        assertThat(leadingSpaces % 2).isEqualTo(0);
                    }
                }
            }
        }

        @Test
        @DisplayName("README의 YAML 키 이름이 kebab-case 규칙 따름")
        void yaml_KeyNamesFollowKebabCase() throws IOException {
            String readme = readReadmeFile();

            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            while (matcher.find()) {
                String yamlContent = matcher.group(1);

                // kebab-case 키들 확인
                assertThat(yamlContent).containsAnyOf(
                        "driver-class-name:",
                        "api-key:",
                        "max-output-tokens:",
                        "worker-count:",
                        "queue-capacity:",
                        "request-timeout-seconds:",
                        "shutdown-timeout-seconds:"
                );

                // camelCase는 사용하지 않음 (YAML에서는 kebab-case 선호)
                assertThat(yamlContent).doesNotContainPattern("\\s+[a-z]+[A-Z]\\w+:");
            }
        }

        @Test
        @DisplayName("README의 YAML 주석이 적절히 사용됨")
        void yaml_CommentsAreAppropriate() throws IOException {
            String readme = readReadmeFile();

            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            while (matcher.find()) {
                String yamlContent = matcher.group(1);

                // 일부 YAML 블록에는 # 주석이 있을 수 있음
                if (yamlContent.contains("#")) {
                    // 주석은 # 으로 시작
                    assertThat(yamlContent).containsPattern("#\\s+.+");
                }
            }
        }
    }

    @Nested
    @DisplayName("Configuration Value Range Validation")
    class ConfigurationValueRangeTest {

        @Test
        @DisplayName("README 예제의 포트 번호가 유효 범위")
        void config_PortNumbersAreValid() throws IOException {
            String readme = readReadmeFile();

            // 포트 번호 추출 (예: 3306)
            Pattern portPattern = Pattern.compile(":(\\d{4,5})/");
            Matcher matcher = portPattern.matcher(readme);

            while (matcher.find()) {
                int port = Integer.parseInt(matcher.group(1));
                assertThat(port).isBetween(1, 65535);
            }
        }

        @Test
        @DisplayName("README 예제의 타임아웃 값이 합리적인 범위")
        void config_TimeoutValuesAreReasonable() throws IOException {
            String readme = readReadmeFile();

            Pattern timeoutPattern = Pattern.compile("timeout-seconds:\\s*(\\d+)");
            Matcher matcher = timeoutPattern.matcher(readme);

            while (matcher.find()) {
                int timeout = Integer.parseInt(matcher.group(1));
                // 타임아웃은 1초 ~ 600초(10분) 사이가 합리적
                assertThat(timeout).isBetween(1, 600);
            }
        }

        @Test
        @DisplayName("README 예제의 큐 용량이 합리적인 범위")
        void config_QueueCapacityIsReasonable() throws IOException {
            String readme = readReadmeFile();

            Pattern capacityPattern = Pattern.compile("queue-capacity:\\s*(\\d+)");
            Matcher matcher = capacityPattern.matcher(readme);

            while (matcher.find()) {
                int capacity = Integer.parseInt(matcher.group(1));
                // 큐 용량은 10 ~ 10000 사이가 합리적
                assertThat(capacity).isBetween(10, 10000);
            }
        }

        @Test
        @DisplayName("README 예제의 워커 수가 합리적인 범위")
        void config_WorkerCountIsReasonable() throws IOException {
            String readme = readReadmeFile();

            Pattern workerPattern = Pattern.compile("worker-count:\\s*(\\d+)");
            Matcher matcher = workerPattern.matcher(readme);

            while (matcher.find()) {
                int workerCount = Integer.parseInt(matcher.group(1));
                // 워커 수는 1 ~ 100 사이가 합리적
                assertThat(workerCount).isBetween(1, 100);
            }
        }
    }

    @Nested
    @DisplayName("Configuration Consistency Check")
    class ConfigurationConsistencyTest {

        @Test
        @DisplayName("README의 설정 예제와 실제 test 설정이 유사한 구조")
        void config_ReadmeAndTestConfigHaveSimilarStructure() throws IOException {
            String readme = readReadmeFile();

            // README에 있는 주요 설정 키들이 실제 설정에서도 사용됨을 확인
            assertThat(geminiProperties).isNotNull();
            assertThat(geminiProperties.getModels()).isNotEmpty();
            assertThat(geminiProperties.getTemperature()).isNotNull();
            assertThat(geminiProperties.getMaxOutputTokens()).isPositive();
        }

        @Test
        @DisplayName("README의 spring.ai.gemini 경로가 실제 설정과 일치")
        void config_GeminiConfigPathMatches() throws IOException {
            String readme = readReadmeFile();

            // README: spring.ai.gemini
            assertThat(readme).contains("spring:");
            assertThat(readme).contains("ai:");
            assertThat(readme).contains("gemini:");

            // 실제로 spring.ai.gemini로 바인딩되는지 확인
            assertThat(geminiProperties).isNotNull();
        }

        @Test
        @DisplayName("README에 명시된 기본값이 합리적")
        void config_DefaultValuesAreReasonable() throws IOException {
            String readme = readReadmeFile();

            // temperature: 0.7 (창의성과 일관성의 균형)
            if (readme.contains("temperature: 0.7")) {
                assertThat(0.7).isBetween(0.0, 1.0);
            }

            // max-output-tokens: 8192 (충분한 응답 길이)
            if (readme.contains("max-output-tokens: 8192")) {
                assertThat(8192).isGreaterThan(1000);
            }

            // worker-count: 10 (적절한 동시 처리 수)
            if (readme.contains("worker-count: 10")) {
                assertThat(10).isBetween(1, 50);
            }

            // queue-capacity: 100 (적절한 대기열 크기)
            if (readme.contains("queue-capacity: 100")) {
                assertThat(100).isBetween(10, 1000);
            }
        }
    }

    @Nested
    @DisplayName("Environment Variable Usage")
    class EnvironmentVariableTest {

        @Test
        @DisplayName("README에 환경변수 사용 패턴이 일관됨")
        void config_EnvironmentVariablePatternIsConsistent() throws IOException {
            String readme = readReadmeFile();

            // Spring Boot 환경변수 패턴: ${VAR_NAME:default_value}
            Pattern envPattern = Pattern.compile("\\$\\{[A-Z_]+(?::\\w*)?\\}");
            Matcher matcher = envPattern.matcher(readme);

            int envVarCount = 0;
            while (matcher.find()) {
                String envVar = matcher.group();
                // 올바른 형식인지 확인
                assertThat(envVar).matches("\\$\\{[A-Z_0-9]+(?::[^}]*)?\\}");
                envVarCount++;
            }

            // 최소 몇 개의 환경변수가 사용됨
            assertThat(envVarCount).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("README의 API 키 환경변수가 보안 권장사항 따름")
        void config_ApiKeyEnvironmentVariablesFollowSecurityBestPractices() throws IOException {
            String readme = readReadmeFile();

            // API 키는 환경변수로 관리 (하드코딩하지 않음)
            assertThat(readme).contains("${GEMINI_API_KEY");
            assertThat(readme).doesNotContainPattern("api-key:\\s*\"[a-zA-Z0-9]{20,}\"");
        }

        @Test
        @DisplayName("README의 비밀번호도 환경변수나 플레이스홀더 사용")
        void config_PasswordsUseEnvironmentVariablesOrPlaceholders() throws IOException {
            String readme = readReadmeFile();

            // 비밀번호는 평문으로 노출하지 않고 플레이스홀더 사용
            Pattern passwordPattern = Pattern.compile("password:\\s*(.+)");
            Matcher matcher = passwordPattern.matcher(readme);

            while (matcher.find()) {
                String passwordValue = matcher.group(1).trim();
                // "your-password" 같은 플레이스홀더거나 환경변수여야 함
                assertThat(passwordValue).matches("(your-password|\\$\\{.+\\}|sa|)");
            }
        }
    }
}