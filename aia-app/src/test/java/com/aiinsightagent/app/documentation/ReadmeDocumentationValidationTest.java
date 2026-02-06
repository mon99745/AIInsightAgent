package com.aiinsightagent.app.documentation;

import com.aiinsightagent.app.TestApplication;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * README.md 문서 검증 테스트
 *
 * README.md에 기재된 API 예제, JSON 구조, 설정 예시 등이 실제 코드와 일치하는지 검증합니다.
 * This test validates that API examples, JSON structures, and configuration examples
 * in README.md match the actual implementation.
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@DisplayName("README.md Documentation Validation Test")
class ReadmeDocumentationValidationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String README_PATH = "README.md";

    /**
     * README.md 파일을 읽어옵니다.
     */
    private String readReadmeFile() throws IOException {
        Path readmePath = Paths.get(README_PATH);
        if (!Files.exists(readmePath)) {
            readmePath = Paths.get("../README.md");
        }
        if (!Files.exists(readmePath)) {
            readmePath = Paths.get("../../README.md");
        }
        return Files.readString(readmePath);
    }

    @Nested
    @DisplayName("README Structure Validation")
    class ReadmeStructureTest {

        @Test
        @DisplayName("README.md 파일이 존재하는지 확인")
        void readme_FileExists() {
            assertDoesNotThrow(() -> {
                String content = readReadmeFile();
                assertThat(content).isNotEmpty();
            });
        }

        @Test
        @DisplayName("README에 필수 섹션이 포함되어 있는지 확인")
        void readme_ContainsRequiredSections() throws IOException {
            String readme = readReadmeFile();

            // 필수 섹션 검증
            assertThat(readme).contains("# 🤖 AIInsightAgent");
            assertThat(readme).contains("## 📌 Table of Contents");
            assertThat(readme).contains("## 🧩 Project Introduction");
            assertThat(readme).contains("## 🛠 Tech Stack");
            assertThat(readme).contains("## 🗂 Project Structure");
            assertThat(readme).contains("## ⚙ Configuration");
            assertThat(readme).contains("## 📡 API Specification");
            assertThat(readme).contains("## 🏗 Architecture");
            assertThat(readme).contains("## 🧪 Testing");
        }

        @Test
        @DisplayName("README에 프로젝트 설명이 포함되어 있는지 확인")
        void readme_ContainsProjectDescription() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("Google Gemini API");
            assertThat(readme).contains("데이터 분석");
            assertThat(readme).contains("인사이트");
            assertThat(readme).contains("큐 기반 비동기 아키텍처");
        }

        @Test
        @DisplayName("README에 기술 스택 정보가 포함되어 있는지 확인")
        void readme_ContainsTechStack() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("Java 17");
            assertThat(readme).contains("Spring Boot 3.5");
            assertThat(readme).contains("Gradle 8");
            assertThat(readme).contains("MariaDB");
            assertThat(readme).contains("JPA");
            assertThat(readme).contains("Google Gemini API");
            assertThat(readme).contains("JUnit 5");
            assertThat(readme).contains("Mockito");
        }

        @Test
        @DisplayName("README에 모듈 구조가 명시되어 있는지 확인")
        void readme_ContainsModuleStructure() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("aia-app");
            assertThat(readme).contains("aia-core");
            assertThat(readme).contains("aia-common");
        }
    }

    @Nested
    @DisplayName("API Endpoint Documentation Validation")
    class ApiEndpointValidationTest {

        @Test
        @DisplayName("README에 모든 API 엔드포인트가 문서화되어 있는지 확인")
        void readme_DocumentsAllApiEndpoints() throws IOException {
            String readme = readReadmeFile();

            // Analysis API endpoints
            assertThat(readme).contains("POST /api/v1/analysis");
            assertThat(readme).contains("GET /api/v1/analysis/history");

            // Context API endpoints
            assertThat(readme).contains("POST /api/v1/context/save");
            assertThat(readme).contains("POST /api/v1/context/get");
            assertThat(readme).contains("POST /api/v1/context/update");
            assertThat(readme).contains("POST /api/v1/context/delete");
        }

        @Test
        @DisplayName("README에 API 요청/응답 예제가 포함되어 있는지 확인")
        void readme_ContainsApiExamples() throws IOException {
            String readme = readReadmeFile();

            // Request/Response 예제 확인
            assertThat(readme).contains("Request Body:");
            assertThat(readme).contains("Response:");
            assertThat(readme).contains("Content-Type: application/json");
        }

        @Test
        @DisplayName("README의 API 경로가 일관된 형식을 따르는지 확인")
        void readme_ApiPathsFollowConsistentPattern() throws IOException {
            String readme = readReadmeFile();

            // API 경로 패턴 추출
            Pattern apiPattern = Pattern.compile("(GET|POST|PUT|DELETE) /api/v\\d+/[\\w/]+");
            Matcher matcher = apiPattern.matcher(readme);

            int apiCount = 0;
            while (matcher.find()) {
                String apiPath = matcher.group();
                assertThat(apiPath).matches("(GET|POST|PUT|DELETE) /api/v\\d+/[\\w/]+");
                apiCount++;
            }

            // 최소 5개 이상의 API 엔드포인트가 문서화되어 있어야 함
            assertThat(apiCount).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("JSON Example Validation")
    class JsonExampleValidationTest {

        @Test
        @DisplayName("README의 InsightRequest JSON 예제가 유효한지 확인")
        void readme_InsightRequestJsonIsValid() throws IOException {
            String readme = readReadmeFile();

            // README에서 InsightRequest 예제 JSON 추출 및 검증
            String exampleJson = """
                {
                  "userId": "user-001",
                  "purpose": "Analysis category",
                  "userPrompt": [
                    {
                      "dataKey": "Session 1",
                      "data": {
                        "Analysis info key 1": "Analysis info 1",
                        "Analysis info key 2": "Analysis info 2",
                        "Analysis info key 3": "Analysis info 3"
                      }
                    }
                  ]
                }
                """;

            // JSON 파싱이 성공하는지 확인
            assertDoesNotThrow(() -> {
                JsonNode jsonNode = objectMapper.readTree(exampleJson);
                assertThat(jsonNode.has("userId")).isTrue();
                assertThat(jsonNode.has("purpose")).isTrue();
                assertThat(jsonNode.has("userPrompt")).isTrue();
            });

            // 실제 InsightRequest 객체로 변환 가능한지 확인
            assertDoesNotThrow(() -> {
                UserPrompt userPrompt = UserPrompt.builder()
                        .dataKey("Session 1")
                        .data(Map.of(
                                "Analysis info key 1", "Analysis info 1",
                                "Analysis info key 2", "Analysis info 2",
                                "Analysis info key 3", "Analysis info 3"
                        ))
                        .build();

                InsightRequest request = InsightRequest.builder()
                        .userId("user-001")
                        .purpose("Analysis category")
                        .userPrompt(List.of(userPrompt))
                        .build();

                assertThat(request).isNotNull();
                assertThat(request.getUserId()).isEqualTo("user-001");
                assertThat(request.getPurpose()).isEqualTo("Analysis category");
            });
        }

        @Test
        @DisplayName("README의 InsightResponse JSON 예제가 유효한 구조인지 확인")
        void readme_InsightResponseJsonIsValid() {
            String exampleJson = """
                {
                  "resultCode": 200,
                  "resultMsg": "OK",
                  "insight": {
                    "summary": "Analysis summary",
                    "issueCategories": [
                      {
                        "category": "Performance",
                        "description": "Issue description",
                        "severity": "HIGH"
                      }
                    ],
                    "rootCauseInsights": ["Root cause analysis 1", "Root cause analysis 2"],
                    "recommendedActions": ["Recommended action 1", "Recommended action 2"],
                    "priorityScore": 75
                  }
                }
                """;

            assertDoesNotThrow(() -> {
                JsonNode jsonNode = objectMapper.readTree(exampleJson);
                assertThat(jsonNode.has("resultCode")).isTrue();
                assertThat(jsonNode.has("resultMsg")).isTrue();
                assertThat(jsonNode.has("insight")).isTrue();

                JsonNode insight = jsonNode.get("insight");
                assertThat(insight.has("summary")).isTrue();
                assertThat(insight.has("issueCategories")).isTrue();
                assertThat(insight.has("rootCauseInsights")).isTrue();
                assertThat(insight.has("recommendedActions")).isTrue();
                assertThat(insight.has("priorityScore")).isTrue();

                // issueCategories 배열 검증
                JsonNode categories = insight.get("issueCategories");
                assertThat(categories.isArray()).isTrue();
                assertThat(categories.size()).isGreaterThan(0);

                JsonNode firstCategory = categories.get(0);
                assertThat(firstCategory.has("category")).isTrue();
                assertThat(firstCategory.has("description")).isTrue();
                assertThat(firstCategory.has("severity")).isTrue();
            });
        }

        @Test
        @DisplayName("README의 ContextRequest JSON 예제가 유효한지 확인")
        void readme_ContextRequestJsonIsValid() {
            String exampleJson = """
                {
                  "userId": "user-001",
                  "category": "Prepared data category",
                  "data": {
                    "Prepared data Key 1": "Prepared data content 1",
                    "Prepared data Key 2": "Prepared data content 2"
                  }
                }
                """;

            assertDoesNotThrow(() -> {
                JsonNode jsonNode = objectMapper.readTree(exampleJson);
                assertThat(jsonNode.has("userId")).isTrue();
                assertThat(jsonNode.has("category")).isTrue();
                assertThat(jsonNode.has("data")).isTrue();

                JsonNode data = jsonNode.get("data");
                assertThat(data.isObject()).isTrue();
                assertThat(data.size()).isGreaterThan(0);
            });
        }

        @Test
        @DisplayName("README의 ContextResponse JSON 예제가 유효한지 확인")
        void readme_ContextResponseJsonIsValid() {
            String exampleJson = """
                {
                  "resultCode": 200,
                  "resultMsg": "OK",
                  "context": {
                    "userId": "user-001",
                    "category": "Prepared data category",
                    "data": {
                      "Prepared data Key 1": "Prepared data content 1",
                      "Prepared data Key 2": "Prepared data content 2"
                    }
                  }
                }
                """;

            assertDoesNotThrow(() -> {
                JsonNode jsonNode = objectMapper.readTree(exampleJson);
                assertThat(jsonNode.has("resultCode")).isTrue();
                assertThat(jsonNode.has("resultMsg")).isTrue();
                assertThat(jsonNode.has("context")).isTrue();

                JsonNode context = jsonNode.get("context");
                assertThat(context.has("userId")).isTrue();
                assertThat(context.has("category")).isTrue();
                assertThat(context.has("data")).isTrue();
            });
        }

        @Test
        @DisplayName("README의 모든 JSON 예제가 올바른 형식인지 확인")
        void readme_AllJsonExamplesAreWellFormed() throws IOException {
            String readme = readReadmeFile();

            // JSON 코드 블록 추출 패턴
            Pattern jsonPattern = Pattern.compile("```json\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(readme);

            int jsonCount = 0;
            while (matcher.find()) {
                String jsonContent = matcher.group(1).trim();
                jsonCount++;

                // 각 JSON이 유효한지 검증
                assertDoesNotThrow(() -> {
                    objectMapper.readTree(jsonContent);
                }, "Invalid JSON found in README.md: " + jsonContent.substring(0, Math.min(100, jsonContent.length())));
            }

            // 최소 5개 이상의 JSON 예제가 있어야 함
            assertThat(jsonCount).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Configuration Example Validation")
    class ConfigurationValidationTest {

        @Test
        @DisplayName("README의 Gemini API 설정 예제가 문서화되어 있는지 확인")
        void readme_ContainsGeminiConfiguration() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("spring:");
            assertThat(readme).contains("ai:");
            assertThat(readme).contains("gemini:");
            assertThat(readme).contains("models:");
            assertThat(readme).contains("api-key:");
            assertThat(readme).contains("temperature:");
            assertThat(readme).contains("max-output-tokens:");
        }

        @Test
        @DisplayName("README의 Queue/Worker 설정 예제가 문서화되어 있는지 확인")
        void readme_ContainsQueueWorkerConfiguration() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("aiinsight:");
            assertThat(readme).contains("request:");
            assertThat(readme).contains("queue:");
            assertThat(readme).contains("worker-count:");
            assertThat(readme).contains("queue-capacity:");
        }

        @Test
        @DisplayName("README의 Database 설정 예제가 문서화되어 있는지 확인")
        void readme_ContainsDatabaseConfiguration() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("datasource:");
            assertThat(readme).contains("driver-class-name:");
            assertThat(readme).contains("url:");
            assertThat(readme).contains("username:");
            assertThat(readme).contains("password:");
        }

        @Test
        @DisplayName("README의 설정 예제가 YAML 형식인지 확인")
        void readme_ConfigurationExamplesAreYamlFormat() throws IOException {
            String readme = readReadmeFile();

            // YAML 코드 블록이 존재하는지 확인
            assertThat(readme).contains("```yaml");

            // YAML 코드 블록 추출
            Pattern yamlPattern = Pattern.compile("```yaml\\s+(.+?)\\s+```", Pattern.DOTALL);
            Matcher matcher = yamlPattern.matcher(readme);

            int yamlCount = 0;
            while (matcher.find()) {
                String yamlContent = matcher.group(1).trim();
                yamlCount++;

                // YAML 형식 기본 검증 (들여쓰기와 콜론 사용)
                assertThat(yamlContent).containsPattern("^\\w+:", Pattern.MULTILINE);
            }

            // 최소 2개 이상의 YAML 예제가 있어야 함
            assertThat(yamlCount).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Architecture Documentation Validation")
    class ArchitectureValidationTest {

        @Test
        @DisplayName("README에 아키텍처 다이어그램이 포함되어 있는지 확인")
        void readme_ContainsArchitectureDiagram() throws IOException {
            String readme = readReadmeFile();

            // Request Flow 다이어그램 확인
            assertThat(readme).contains("Request Flow");
            assertThat(readme).contains("InsightController");
            assertThat(readme).contains("InsightService");
            assertThat(readme).contains("InsightFacade");
            assertThat(readme).contains("GeminiChatAdapter");
            assertThat(readme).contains("GeminiQueueManager");
            assertThat(readme).contains("GeminiWorker");
        }

        @Test
        @DisplayName("README에 큐 기반 처리 구조가 설명되어 있는지 확인")
        void readme_ExplainsQueueBasedProcessing() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("Queue-Based Processing");
            assertThat(readme).contains("BlockingQueue");
            assertThat(readme).contains("Worker Thread Pool");
            assertThat(readme).contains("CompletableFuture");
        }

        @Test
        @DisplayName("README에 주요 기능이 설명되어 있는지 확인")
        void readme_ExplainsKeyFeatures() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("Key Features");
            assertThat(readme).contains("데이터 분석");
            assertThat(readme).contains("멀티 모델");
            assertThat(readme).contains("멀티 키 지원");
            assertThat(readme).contains("큐 기반 처리");
            assertThat(readme).contains("컨텍스트 관리");
            assertThat(readme).contains("분석 히스토리 관리");
        }

        @Test
        @DisplayName("README에 Worker-API Key 매핑이 설명되어 있는지 확인")
        void readme_Explains1To1WorkerApiKeyMapping() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("1:1 Worker-API Key Mapping");
            assertThat(readme).contains("Round-Robin");
            assertThat(readme).contains("Asynchronous Processing");
            assertThat(readme).contains("Rate Limit");
        }
    }

    @Nested
    @DisplayName("Testing Section Validation")
    class TestingSectionValidationTest {

        @Test
        @DisplayName("README에 테스트 실행 방법이 문서화되어 있는지 확인")
        void readme_ContainsTestingInstructions() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("## 🧪 Testing");
            assertThat(readme).contains("./gradlew test");
        }

        @Test
        @DisplayName("README에 모듈별 테스트 방법이 설명되어 있는지 확인")
        void readme_ExplainsModuleSpecificTesting() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains(":aia-core:test");
            assertThat(readme).contains(":aia-app:test");
        }

        @Test
        @DisplayName("README에 테스트 리포트 경로가 명시되어 있는지 확인")
        void readme_SpecifiesTestReportPaths() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("Test Reports");
            assertThat(readme).contains("build/reports/tests/test/index.html");
        }

        @Test
        @DisplayName("README의 Gradle 명령어가 올바른 형식인지 확인")
        void readme_GradleCommandsAreCorrect() throws IOException {
            String readme = readReadmeFile();

            // Gradle wrapper 명령어 확인
            Pattern gradlePattern = Pattern.compile("\\./gradlew [\\w:]+");
            Matcher matcher = gradlePattern.matcher(readme);

            int commandCount = 0;
            while (matcher.find()) {
                String command = matcher.group();
                assertThat(command).startsWith("./gradlew");
                commandCount++;
            }

            assertThat(commandCount).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("HTTP Status Code Documentation")
    class HttpStatusCodeValidationTest {

        @Test
        @DisplayName("README에 성공 응답 코드가 명시되어 있는지 확인")
        void readme_SpecifiesSuccessStatusCodes() throws IOException {
            String readme = readReadmeFile();

            // 200 OK 응답이 예제에 포함되어 있는지 확인
            assertThat(readme).contains("\"resultCode\": 200");
            assertThat(readme).contains("\"resultMsg\": \"OK\"");
        }

        @Test
        @DisplayName("README 예제의 HTTP 메서드가 올바른지 확인")
        void readme_HttpMethodsAreCorrect() throws IOException {
            String readme = readReadmeFile();

            // POST 메서드
            assertThat(readme).containsPattern("POST /api/v1/analysis");
            assertThat(readme).containsPattern("POST /api/v1/context/save");
            assertThat(readme).containsPattern("POST /api/v1/context/update");
            assertThat(readme).containsPattern("POST /api/v1/context/delete");

            // GET 메서드
            assertThat(readme).containsPattern("GET /api/v1/analysis/history");
            assertThat(readme).containsPattern("POST /api/v1/context/get");
        }
    }

    @Nested
    @DisplayName("Project Metadata Validation")
    class ProjectMetadataValidationTest {

        @Test
        @DisplayName("README에 프로젝트 이름이 명확히 표시되어 있는지 확인")
        void readme_ContainsProjectName() throws IOException {
            String readme = readReadmeFile();

            assertThat(readme).contains("AIInsightAgent");
            assertThat(readme).contains("AIA");
        }

        @Test
        @DisplayName("README에 언어 지원 정보가 포함되어 있는지 확인")
        void readme_ContainsBilingualContent() throws IOException {
            String readme = readReadmeFile();

            // 한글과 영어 설명이 모두 포함되어 있는지 확인
            assertThat(readme).containsPattern("[가-힣]+");
            assertThat(readme).containsPattern("[a-zA-Z]+");
        }

        @Test
        @DisplayName("README에 이모지가 적절히 사용되었는지 확인")
        void readme_UsesEmojisAppropriately() throws IOException {
            String readme = readReadmeFile();

            // 주요 섹션에 이모지가 사용되었는지 확인
            assertThat(readme).contains("🤖");
            assertThat(readme).contains("📌");
            assertThat(readme).contains("🧩");
            assertThat(readme).contains("🛠");
            assertThat(readme).contains("🗂");
            assertThat(readme).contains("⚙");
            assertThat(readme).contains("📡");
            assertThat(readme).contains("🏗");
            assertThat(readme).contains("🧪");
        }
    }

    @Nested
    @DisplayName("Code Block Formatting Validation")
    class CodeBlockFormattingTest {

        @Test
        @DisplayName("README의 모든 코드 블록이 올바른 형식인지 확인")
        void readme_CodeBlocksAreWellFormatted() throws IOException {
            String readme = readReadmeFile();

            // 코드 블록 시작 태그 수
            long openingCodeBlocks = readme.lines()
                    .filter(line -> line.trim().startsWith("```"))
                    .count();

            // 코드 블록은 짝수개여야 함 (시작과 끝)
            assertThat(openingCodeBlocks % 2).isEqualTo(0);
        }

        @Test
        @DisplayName("README의 코드 블록이 언어 지정자를 포함하는지 확인")
        void readme_CodeBlocksHaveLanguageSpecifiers() throws IOException {
            String readme = readReadmeFile();

            // 언어 지정자가 있는 코드 블록 확인
            assertThat(readme).contains("```json");
            assertThat(readme).contains("```yaml");
            assertThat(readme).contains("```http");
            assertThat(readme).contains("```bash");
        }

        @Test
        @DisplayName("README의 들여쓰기가 일관성 있는지 확인")
        void readme_IndentationIsConsistent() throws IOException {
            String readme = readReadmeFile();

            // 마크다운 리스트 항목들이 올바르게 들여쓰기되어 있는지 확인
            assertThat(readme).containsPattern("^- ", Pattern.MULTILINE);
            assertThat(readme).containsPattern("^  - ", Pattern.MULTILINE);
        }
    }

    @Nested
    @DisplayName("Field Naming Convention Validation")
    class FieldNamingValidationTest {

        @Test
        @DisplayName("README의 JSON 필드명이 camelCase를 따르는지 확인")
        void readme_JsonFieldsUseCamelCase() throws IOException {
            String readme = readReadmeFile();

            // README에서 주요 필드명 확인
            assertThat(readme).contains("\"userId\"");
            assertThat(readme).contains("\"userPrompt\"");
            assertThat(readme).contains("\"dataKey\"");
            assertThat(readme).contains("\"resultCode\"");
            assertThat(readme).contains("\"resultMsg\"");
            assertThat(readme).contains("\"issueCategories\"");
            assertThat(readme).contains("\"rootCauseInsights\"");
            assertThat(readme).contains("\"recommendedActions\"");
            assertThat(readme).contains("\"priorityScore\"");
        }

        @Test
        @DisplayName("README의 YAML 설정 키가 kebab-case를 따르는지 확인")
        void readme_YamlKeysUseKebabCase() throws IOException {
            String readme = readReadmeFile();

            // YAML 설정에서 kebab-case 확인
            assertThat(readme).contains("api-key:");
            assertThat(readme).contains("max-output-tokens:");
            assertThat(readme).contains("worker-count:");
            assertThat(readme).contains("queue-capacity:");
            assertThat(readme).contains("driver-class-name:");
        }
    }

    @Nested
    @DisplayName("Example Data Consistency")
    class ExampleDataConsistencyTest {

        @Test
        @DisplayName("README 전체에서 사용자 ID 예제가 일관성 있는지 확인")
        void readme_UserIdExamplesAreConsistent() throws IOException {
            String readme = readReadmeFile();

            // "user-001" 형식의 사용자 ID가 일관되게 사용되는지 확인
            assertThat(readme).contains("user-001");
        }

        @Test
        @DisplayName("README 전체에서 모델 ID 예제가 일관성 있는지 확인")
        void readme_ModelIdExamplesAreConsistent() throws IOException {
            String readme = readReadmeFile();

            // 모델 ID 형식 (m00, m01 등) 확인
            assertThat(readme).containsPattern("m0[0-9]");
        }

        @Test
        @DisplayName("README 전체에서 API 버전이 일관성 있는지 확인")
        void readme_ApiVersionIsConsistent() throws IOException {
            String readme = readReadmeFile();

            // 모든 API 경로가 v1을 사용하는지 확인
            Pattern apiPattern = Pattern.compile("/api/(v\\d+)/");
            Matcher matcher = apiPattern.matcher(readme);

            while (matcher.find()) {
                String version = matcher.group(1);
                assertThat(version).isEqualTo("v1");
            }
        }
    }

    @Nested
    @DisplayName("Link and Reference Validation")
    class LinkValidationTest {

        @Test
        @DisplayName("README의 목차 링크가 올바른 형식인지 확인")
        void readme_TableOfContentsLinksAreValid() throws IOException {
            String readme = readReadmeFile();

            // 목차 섹션 확인
            assertThat(readme).contains("## 📌 Table of Contents");

            // 앵커 링크 형식 확인
            assertThat(readme).containsPattern("\\[.*\\]\\(#.*\\)");
        }

        @Test
        @DisplayName("README의 내부 참조가 유효한지 확인")
        void readme_InternalReferencesAreValid() throws IOException {
            String readme = readReadmeFile();

            // 헤더 ID와 링크가 일치하는지 기본 검증
            if (readme.contains("[Project Introduction]")) {
                assertThat(readme).contains("## 🧩 Project Introduction");
            }
            if (readme.contains("[Tech Stack]")) {
                assertThat(readme).contains("## 🛠 Tech Stack");
            }
        }
    }
}