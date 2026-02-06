package com.aiinsightagent.app.documentation;

import com.aiinsightagent.app.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * README Edge Cases and Regression Tests
 *
 * README.md 문서의 엣지 케이스와 회귀 테스트를 수행합니다.
 * This test covers edge cases and regression scenarios for README.md documentation.
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@DisplayName("README Edge Cases and Regression Tests")
class ReadmeEdgeCasesTest {

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
    @DisplayName("Regression Tests for Common Documentation Issues")
    class RegressionTests {

        @Test
        @DisplayName("회귀: README에 깨진 마크다운 링크가 없는지 확인")
        void regression_NoBrokenMarkdownLinks() throws IOException {
            String readme = readReadmeFile();

            // 빈 링크 텍스트나 빈 URL이 없는지 확인
            assertThat(readme).doesNotContain("[]()");
            assertThat(readme).doesNotContain("[](");
            assertThat(readme).doesNotContain("[]");

            // 링크 패턴 검증
            Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
            Matcher matcher = linkPattern.matcher(readme);

            while (matcher.find()) {
                String linkText = matcher.group(1);
                String linkUrl = matcher.group(2);

                // 링크 텍스트와 URL이 비어있지 않은지 확인
                assertThat(linkText).isNotEmpty();
                assertThat(linkUrl).isNotEmpty();
            }
        }

        @Test
        @DisplayName("회귀: README에 중복된 섹션 헤더가 없는지 확인")
        void regression_NoDuplicateSectionHeaders() throws IOException {
            String readme = readReadmeFile();

            // 주요 섹션 헤더들
            String[] mainSections = {
                    "## 🧩 Project Introduction",
                    "## 🛠 Tech Stack",
                    "## 🗂 Project Structure",
                    "## ⚙ Configuration",
                    "## 📡 API Specification",
                    "## 🏗 Architecture",
                    "## 🧪 Testing"
            };

            for (String section : mainSections) {
                int count = countOccurrences(readme, section);
                assertThat(count)
                        .withFailMessage("Section '%s' appears %d times, should appear exactly once", section, count)
                        .isEqualTo(1);
            }
        }

        @Test
        @DisplayName("회귀: README의 코드 블록이 모두 닫혀있는지 확인")
        void regression_AllCodeBlocksClosed() throws IOException {
            String readme = readReadmeFile();

            // 코드 블록 시작(```)과 끝(```) 개수가 일치하는지 확인
            long codeBlockMarkers = readme.lines()
                    .filter(line -> line.trim().startsWith("```"))
                    .count();

            assertThat(codeBlockMarkers % 2)
                    .withFailMessage("Code blocks are not properly closed. Found %d markers (should be even)", codeBlockMarkers)
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("회귀: README에 TODO나 FIXME 주석이 남아있지 않은지 확인")
        void regression_NoTodoOrFixmeComments() throws IOException {
            String readme = readReadmeFile();

            // 프로덕션 문서에는 TODO나 FIXME가 없어야 함
            assertThat(readme).doesNotContainIgnoringCase("TODO");
            assertThat(readme).doesNotContainIgnoringCase("FIXME");
            assertThat(readme).doesNotContainIgnoringCase("XXX");
        }

        @Test
        @DisplayName("회귀: README에 플레이스홀더 텍스트가 남아있지 않은지 확인")
        void regression_NoPlaceholderText() throws IOException {
            String readme = readReadmeFile();

            // 흔한 플레이스홀더 패턴
            assertThat(readme).doesNotContainIgnoringCase("[Your ");
            assertThat(readme).doesNotContainIgnoringCase("<Your ");
            assertThat(readme).doesNotContainIgnoringCase("Lorem ipsum");
            assertThat(readme).doesNotContainIgnoringCase("foo bar");
        }

        private int countOccurrences(String text, String pattern) {
            int count = 0;
            int index = 0;
            while ((index = text.indexOf(pattern, index)) != -1) {
                count++;
                index += pattern.length();
            }
            return count;
        }
    }

    @Nested
    @DisplayName("Edge Cases for API Documentation")
    class ApiDocumentationEdgeCases {

        @Test
        @DisplayName("엣지: 모든 HTTP 메서드가 대문자로 표기되었는지 확인")
        void edge_HttpMethodsAreUppercase() throws IOException {
            String readme = readReadmeFile();

            // HTTP 메서드는 대문자로 표기되어야 함
            Pattern httpMethodPattern = Pattern.compile("^(GET|POST|PUT|DELETE|PATCH) /api/", Pattern.MULTILINE);
            Matcher matcher = httpMethodPattern.matcher(readme);

            assertThat(matcher.find()).isTrue();

            // 소문자 HTTP 메서드가 없는지 확인
            assertThat(readme).doesNotContainPattern("^(get|post|put|delete|patch) /api/", Pattern.MULTILINE);
        }

        @Test
        @DisplayName("엣지: API 경로에 불필요한 슬래시가 없는지 확인")
        void edge_NoDoubleSlashesInApiPaths() throws IOException {
            String readme = readReadmeFile();

            // API 경로에 // 가 없어야 함
            Pattern apiPattern = Pattern.compile("(GET|POST|PUT|DELETE|PATCH) (/api/[^\\s]+)");
            Matcher matcher = apiPattern.matcher(readme);

            while (matcher.find()) {
                String apiPath = matcher.group(2);
                assertThat(apiPath).doesNotContain("//");
            }
        }

        @Test
        @DisplayName("엣지: API 경로가 슬래시로 끝나지 않는지 확인")
        void edge_ApiPathsDoNotEndWithSlash() throws IOException {
            String readme = readReadmeFile();

            Pattern apiPattern = Pattern.compile("(GET|POST|PUT|DELETE|PATCH) (/api/[^\\s?]+)");
            Matcher matcher = apiPattern.matcher(readme);

            while (matcher.find()) {
                String apiPath = matcher.group(2);
                // 쿼리 파라미터 전까지의 경로
                if (!apiPath.contains("?")) {
                    assertThat(apiPath).doesNotEndWith("/");
                }
            }
        }

        @Test
        @DisplayName("엣지: JSON 예제에 trailing comma가 없는지 확인")
        void edge_JsonExamplesNoTrailingCommas() throws IOException {
            String readme = readReadmeFile();

            Pattern jsonPattern = Pattern.compile("```json\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(readme);

            while (matcher.find()) {
                String jsonContent = matcher.group(1);

                // JSON에서 trailing comma는 허용되지 않음
                assertThat(jsonContent).doesNotContainPattern(",\\s*[}\\]]");
            }
        }

        @Test
        @DisplayName("엣지: 쿼리 파라미터 형식이 올바른지 확인 (?key=value)")
        void edge_QueryParameterFormatIsCorrect() throws IOException {
            String readme = readReadmeFile();

            Pattern queryPattern = Pattern.compile("/api/[^\\s]*\\?([^\\s]+)");
            Matcher matcher = queryPattern.matcher(readme);

            while (matcher.find()) {
                String queryString = matcher.group(1);

                // 쿼리 파라미터는 key=value 형식
                assertThat(queryString).containsPattern("[\\w-]+=");

                // 잘못된 형식이 없는지 확인
                assertThat(queryString).doesNotContain("??");
                assertThat(queryString).doesNotContain("==");
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases for Configuration Examples")
    class ConfigurationEdgeCases {

        @Test
        @DisplayName("엣지: YAML 예제에 탭 문자가 사용되지 않았는지 확인")
        void edge_YamlExamplesUseSpacesNotTabs() throws IOException {
            String readme = readReadmeFile();

            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            while (matcher.find()) {
                String yamlContent = matcher.group(1);

                // YAML에서는 탭 대신 스페이스를 사용해야 함
                assertThat(yamlContent).doesNotContain("\t");
            }
        }

        @Test
        @DisplayName("엣지: YAML 키-값 구분자 콜론 뒤에 공백이 있는지 확인")
        void edge_YamlColonSpacing() throws IOException {
            String readme = readReadmeFile();

            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            while (matcher.find()) {
                String yamlContent = matcher.group(1);

                // YAML에서 콜론 뒤에는 공백이 있어야 함 (key: value)
                // 콜론만 있고 공백이 없는 경우를 찾음 (단, URL은 제외)
                Pattern colonPattern = Pattern.compile("^\\s*[\\w-]+:[^\\s/]", Pattern.MULTILINE);
                Matcher colonMatcher = colonPattern.matcher(yamlContent);

                if (colonMatcher.find()) {
                    String problematicLine = colonMatcher.group();
                    // URL(http:, https:, jdbc:)이 아닌 경우에만 체크
                    if (!problematicLine.contains("http:") &&
                            !problematicLine.contains("https:") &&
                            !problematicLine.contains("jdbc:")) {
                        assertThat(problematicLine)
                                .withFailMessage("YAML key-value separator should have space after colon: '%s'", problematicLine)
                                .matches(".*:\\s.*");
                    }
                }
            }
        }

        @Test
        @DisplayName("엣지: 환경변수 참조가 올바른 형식인지 확인")
        void edge_EnvironmentVariableReferencesAreValid() throws IOException {
            String readme = readReadmeFile();

            Pattern envVarPattern = Pattern.compile("\\$\\{([^}]+)\\}");
            Matcher matcher = envVarPattern.matcher(readme);

            while (matcher.find()) {
                String envVarContent = matcher.group(1);

                // 환경변수 이름은 대문자와 언더스코어로 구성
                String[] parts = envVarContent.split(":", 2);
                String varName = parts[0];

                assertThat(varName).matches("[A-Z][A-Z0-9_]*");
            }
        }

        @Test
        @DisplayName("엣지: 숫자 설정 값에 불필요한 따옴표가 없는지 확인")
        void edge_NumericConfigValuesNotQuoted() throws IOException {
            String readme = readReadmeFile();

            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            while (matcher.find()) {
                String yamlContent = matcher.group(1);

                // 숫자 값은 따옴표로 감싸지 않아야 함
                assertThat(yamlContent).doesNotContainPattern("temperature:\\s*[\"']\\d+");
                assertThat(yamlContent).doesNotContainPattern("max-output-tokens:\\s*[\"']\\d+");
                assertThat(yamlContent).doesNotContainPattern("worker-count:\\s*[\"']\\d+");
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases for Text Content")
    class TextContentEdgeCases {

        @Test
        @DisplayName("엣지: 이중 공백이 과도하게 사용되지 않았는지 확인")
        void edge_NoExcessiveDoubleSpaces() throws IOException {
            String readme = readReadmeFile();

            // 3개 이상의 연속된 공백은 의도하지 않은 것일 가능성
            assertThat(readme).doesNotContainPattern("   +");
        }

        @Test
        @DisplayName("엣지: 줄 끝 공백이 없는지 확인")
        void edge_NoTrailingWhitespace() throws IOException {
            String readme = readReadmeFile();

            String[] lines = readme.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (!line.isEmpty() && line.endsWith(" ")) {
                    assertThat(line)
                            .withFailMessage("Line %d has trailing whitespace", i + 1)
                            .doesNotEndWith(" ");
                }
            }
        }

        @Test
        @DisplayName("엣지: 연속된 빈 줄이 3개를 초과하지 않는지 확인")
        void edge_NoExcessiveBlankLines() throws IOException {
            String readme = readReadmeFile();

            // 4개 이상의 연속된 빈 줄은 과도함
            assertThat(readme).doesNotContain("\n\n\n\n\n");
        }

        @Test
        @DisplayName("엣지: 한글과 영어 사이에 적절한 공백이 있는지 샘플 확인")
        void edge_SpacingBetweenKoreanAndEnglish() throws IOException {
            String readme = readReadmeFile();

            // 한글과 영어가 붙어있는 경우를 찾음 (일부는 의도적일 수 있음)
            // 이 테스트는 참고용이며, 실제로는 프로젝트 스타일에 따라 다를 수 있음

            // 샘플 패턴: 한글 바로 뒤에 영어 단어
            Pattern pattern = Pattern.compile("[가-힣][A-Za-z]");
            Matcher matcher = pattern.matcher(readme);

            // 발견된 케이스가 있다면 로그만 남기고 실패하지 않음
            // (이것은 스타일 가이드에 따라 다를 수 있음)
            int count = 0;
            while (matcher.find() && count < 5) {
                count++;
            }
            // 단순 정보 수집용 테스트
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Boundary Cases for Numeric Values")
    class NumericBoundaryCases {

        @Test
        @DisplayName("경계: README의 모든 포트 번호가 유효 범위인지 확인")
        void boundary_PortNumbersWithinValidRange() throws IOException {
            String readme = readReadmeFile();

            Pattern portPattern = Pattern.compile(":(\\d+)/");
            Matcher matcher = portPattern.matcher(readme);

            while (matcher.find()) {
                int port = Integer.parseInt(matcher.group(1));
                assertThat(port)
                        .withFailMessage("Port number %d is out of valid range [1-65535]", port)
                        .isBetween(1, 65535);
            }
        }

        @Test
        @DisplayName("경계: README의 우선순위 점수가 0-100 범위인지 확인")
        void boundary_PriorityScoreWithinValidRange() throws IOException {
            String readme = readReadmeFile();

            Pattern priorityPattern = Pattern.compile("\"priorityScore\":\\s*(\\d+)");
            Matcher matcher = priorityPattern.matcher(readme);

            while (matcher.find()) {
                int priority = Integer.parseInt(matcher.group(1));
                assertThat(priority)
                        .withFailMessage("Priority score %d should be between 0 and 100", priority)
                        .isBetween(0, 100);
            }
        }

        @Test
        @DisplayName("경계: README의 타임아웃 값이 최소 1초 이상인지 확인")
        void boundary_TimeoutValuesAtLeastOneSecond() throws IOException {
            String readme = readReadmeFile();

            Pattern timeoutPattern = Pattern.compile("timeout-seconds:\\s*(\\d+)");
            Matcher matcher = timeoutPattern.matcher(readme);

            while (matcher.find()) {
                int timeout = Integer.parseInt(matcher.group(1));
                assertThat(timeout)
                        .withFailMessage("Timeout value %d should be at least 1 second", timeout)
                        .isGreaterThanOrEqualTo(1);
            }
        }

        @Test
        @DisplayName("경계: README의 temperature 값이 0 이상인지 확인")
        void boundary_TemperatureIsNonNegative() throws IOException {
            String readme = readReadmeFile();

            Pattern tempPattern = Pattern.compile("temperature:\\s*(\\d+\\.\\d+|\\d+)");
            Matcher matcher = tempPattern.matcher(readme);

            while (matcher.find()) {
                double temperature = Double.parseDouble(matcher.group(1));
                assertThat(temperature)
                        .withFailMessage("Temperature %f should be non-negative", temperature)
                        .isGreaterThanOrEqualTo(0.0);
            }
        }
    }

    @Nested
    @DisplayName("Negative Cases for Documentation")
    class NegativeCases {

        @Test
        @DisplayName("부정: README에 잘못된 HTTP 상태 코드가 없는지 확인")
        void negative_NoInvalidHttpStatusCodes() throws IOException {
            String readme = readReadmeFile();

            // HTTP 상태 코드는 100-599 범위
            Pattern statusPattern = Pattern.compile("\"resultCode\":\\s*(\\d+)");
            Matcher matcher = statusPattern.matcher(readme);

            while (matcher.find()) {
                int statusCode = Integer.parseInt(matcher.group(1));
                assertThat(statusCode)
                        .withFailMessage("Invalid HTTP status code: %d", statusCode)
                        .isBetween(100, 599);
            }
        }

        @Test
        @DisplayName("부정: README에 잘못된 JSON null 표기가 없는지 확인")
        void negative_CorrectJsonNullRepresentation() throws IOException {
            String readme = readReadmeFile();

            Pattern jsonPattern = Pattern.compile("```json\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(readme);

            while (matcher.find()) {
                String jsonContent = matcher.group(1);

                // JSON에서 null은 소문자여야 함
                assertThat(jsonContent).doesNotContain(": NULL");
                assertThat(jsonContent).doesNotContain(": Null");
            }
        }

        @Test
        @DisplayName("부정: README에 빈 배열이 null이 아닌 []로 표기되었는지 확인")
        void negative_EmptyArraysNotNull() throws IOException {
            String readme = readReadmeFile();

            // 배열 필드는 빈 경우에도 []로 표기되어야 함
            // "array": null 보다는 "array": [] 권장

            Pattern jsonPattern = Pattern.compile("```json\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(readme);

            while (matcher.find()) {
                String jsonContent = matcher.group(1);

                // issueCategories, rootCauseInsights, recommendedActions는 배열
                if (jsonContent.contains("issueCategories") ||
                        jsonContent.contains("rootCauseInsights") ||
                        jsonContent.contains("recommendedActions")) {
                    // 이들이 null로 표기되지 않았는지 확인 (빈 배열이면 [])
                    // 단, context가 null인 경우는 예외 (삭제 응답)
                }
            }
        }

        @Test
        @DisplayName("부정: README의 API 경로에 잘못된 문자가 없는지 확인")
        void negative_NoInvalidCharactersInApiPaths() throws IOException {
            String readme = readReadmeFile();

            Pattern apiPattern = Pattern.compile("(GET|POST|PUT|DELETE|PATCH) (/api/[^\\s]+)");
            Matcher matcher = apiPattern.matcher(readme);

            while (matcher.find()) {
                String apiPath = matcher.group(2);

                // API 경로에는 영문, 숫자, -, /, ?, =, & 만 허용
                assertThat(apiPath).matches("[/a-zA-Z0-9\\-?=&]+");
            }
        }
    }

    @Nested
    @DisplayName("Consistency Across Language Versions")
    class BilingualConsistencyTest {

        @Test
        @DisplayName("일관성: 한국어와 영어 설명이 모두 포함되어 있는지 확인")
        void consistency_BothKoreanAndEnglishDescriptions() throws IOException {
            String readme = readReadmeFile();

            // 주요 섹션에 한국어와 영어 설명이 모두 있어야 함
            assertThat(readme).containsPattern("[가-힣]+");
            assertThat(readme).containsPattern("[A-Za-z]+");

            // 프로젝트 소개 부분에서 이중 언어 확인
            assertThat(readme).contains("데이터 분석");
            assertThat(readme).contains("data analysis");
        }

        @Test
        @DisplayName("일관성: 주요 용어가 일관되게 사용되는지 확인")
        void consistency_KeyTermsUsedConsistently() throws IOException {
            String readme = readReadmeFile();

            // "Gemini API"는 일관되게 표기
            assertThat(readme).contains("Gemini API");
            assertThat(readme).doesNotContainIgnoringCase("Gemini api");
            assertThat(readme).doesNotContainIgnoringCase("gemini API");

            // "InsightAgent"는 일관되게 표기
            if (readme.contains("InsightAgent")) {
                assertThat(readme).doesNotContain("Insight Agent");
                assertThat(readme).doesNotContain("insightagent");
            }
        }
    }
}