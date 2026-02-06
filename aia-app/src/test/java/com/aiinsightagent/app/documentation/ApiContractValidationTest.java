package com.aiinsightagent.app.documentation;

import com.aiinsightagent.app.TestApplication;
import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.repository.ActorRepository;
import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.aiinsightagent.core.queue.GeminiQueueManager;
import com.aiinsightagent.core.queue.GeminiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Contract Validation Test
 *
 * README.md에 문서화된 API 스펙과 실제 구현된 API가 일치하는지 검증합니다.
 * This test validates that the implemented APIs match the specifications documented in README.md.
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API Contract Validation - README.md vs Implementation")
class ApiContractValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActorRepository actorRepository;

    @MockitoBean
    private Client geminiClient;

    @MockitoBean
    private Models geminiModels;

    @MockitoBean
    private GeminiQueueManager geminiQueueManager;

    @MockitoBean
    private GeminiChatAdapter geminiChatAdapter;

    @BeforeEach
    void setUp() {
        // Mock Gemini response
        String mockJsonResponse = """
            {
                "summary": "Test analysis summary",
                "issueCategories": [
                    {
                        "category": "Performance",
                        "description": "Test issue",
                        "severity": "MEDIUM"
                    }
                ],
                "rootCauseInsights": ["Root cause 1", "Root cause 2"],
                "recommendedActions": ["Action 1", "Action 2"],
                "priorityScore": 75
            }
            """;

        GenerateContentResponse mockResponse = org.mockito.Mockito.mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn(mockJsonResponse);

        GenerateContentResponseUsageMetadata mockUsage = org.mockito.Mockito.mock(GenerateContentResponseUsageMetadata.class);
        when(mockUsage.promptTokenCount()).thenReturn(Optional.of(100));
        when(mockUsage.candidatesTokenCount()).thenReturn(Optional.of(50));
        when(mockUsage.totalTokenCount()).thenReturn(Optional.of(150));
        when(mockResponse.usageMetadata()).thenReturn(Optional.of(mockUsage));

        GeminiResponse geminiResponse = new GeminiResponse(mockResponse, "m01", "gemini-2.5-flash");
        when(geminiChatAdapter.getResponse(anyString())).thenReturn(geminiResponse);

        // Create test actor
        Actor actor = Actor.create("user-001");
        actorRepository.save(actor);
    }

    @Nested
    @DisplayName("POST /api/v1/analysis - Data Analysis API")
    class AnalysisApiContractTest {

        @Test
        @DisplayName("README 예제 요청 형식대로 API 호출 시 성공")
        void analysisApi_WithReadmeExampleRequest_Success() throws Exception {
            // given - README.md의 예제 JSON과 동일한 구조
            Map<String, String> data = new HashMap<>();
            data.put("Analysis info key 1", "Analysis info 1");
            data.put("Analysis info key 2", "Analysis info 2");
            data.put("Analysis info key 3", "Analysis info 3");

            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("Session 1")
                    .data(data)
                    .build();

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("Analysis category")
                    .userPrompt(List.of(userPrompt))
                    .build();

            String requestBody = objectMapper.writeValueAsString(request);

            // when & then
            MvcResult result = mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            // Verify response structure matches README
            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("resultCode");
            assertThat(responseBody).contains("resultMsg");
            assertThat(responseBody).contains("insight");
        }

        @Test
        @DisplayName("README 예제 응답 형식이 실제 응답과 일치")
        void analysisApi_ResponseMatchesReadmeFormat() throws Exception {
            // given
            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("Session 1")
                    .data(Map.of("key", "value"))
                    .build();

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("test")
                    .userPrompt(List.of(userPrompt))
                    .build();

            // when & then - README에 명시된 응답 구조 검증
            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value(200))
                    .andExpect(jsonPath("$.resultMsg").value("OK"))
                    .andExpect(jsonPath("$.insight").exists())
                    .andExpect(jsonPath("$.insight.summary").exists())
                    .andExpect(jsonPath("$.insight.issueCategories").isArray())
                    .andExpect(jsonPath("$.insight.rootCauseInsights").isArray())
                    .andExpect(jsonPath("$.insight.recommendedActions").isArray())
                    .andExpect(jsonPath("$.insight.priorityScore").exists());
        }

        @Test
        @DisplayName("README에 명시된 Content-Type 검증")
        void analysisApi_RequiresJsonContentType() throws Exception {
            // given
            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("test")
                    .data(Map.of("key", "value"))
                    .build();

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("test")
                    .userPrompt(List.of(userPrompt))
                    .build();

            // when & then - Content-Type이 application/json이어야 함
            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Content-Type 없으면 415 에러
            mockMvc.perform(post("/api/v1/analysis")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("README에 명시된 필수 필드 검증 (userId, purpose, userPrompt)")
        void analysisApi_RequiredFieldsValidation() throws Exception {
            // given - 모든 필수 필드 포함
            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("test")
                    .data(Map.of("key", "value"))
                    .build();

            InsightRequest validRequest = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("test_purpose")
                    .userPrompt(List.of(userPrompt))
                    .build();

            // when & then - 정상 요청
            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("README 예제처럼 여러 UserPrompt 배열 처리 검증")
        void analysisApi_HandlesMultipleUserPrompts() throws Exception {
            // given - README 예제처럼 2개의 UserPrompt
            List<UserPrompt> userPrompts = List.of(
                    UserPrompt.builder()
                            .dataKey("Session 1")
                            .data(Map.of(
                                    "Analysis info key 1", "Analysis info 1",
                                    "Analysis info key 2", "Analysis info 2"
                            ))
                            .build(),
                    UserPrompt.builder()
                            .dataKey("Session 2")
                            .data(Map.of(
                                    "Analysis info key 1", "Analysis info 1",
                                    "Analysis info key 2", "Analysis info 2"
                            ))
                            .build()
            );

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("Analysis category")
                    .userPrompt(userPrompts)
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/analysis/history - Analysis History API")
    class HistoryApiContractTest {

        @Test
        @DisplayName("README 예제처럼 userId 쿼리 파라미터로 이력 조회")
        void historyApi_WithUserIdQueryParam_Success() throws Exception {
            // when & then - README: GET /api/v1/analysis/history?userId=user-001
            mockMvc.perform(get("/api/v1/analysis/history")
                            .param("userId", "user-001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value(200))
                    .andExpect(jsonPath("$.resultMsg").exists())
                    .andExpect(jsonPath("$.insightRecords").isArray());
        }

        @Test
        @DisplayName("README에 명시된 대로 GET 메서드만 허용")
        void historyApi_OnlyGetMethodAllowed() throws Exception {
            // GET - Success
            mockMvc.perform(get("/api/v1/analysis/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk());

            // POST - Method Not Allowed
            mockMvc.perform(post("/api/v1/analysis/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("README 예제처럼 insightRecords 배열 응답")
        void historyApi_ReturnsInsightRecordsArray() throws Exception {
            // when & then
            MvcResult result = mockMvc.perform(get("/api/v1/analysis/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.insightRecords").isArray())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            assertThat(response).contains("insightRecords");
        }

        @Test
        @DisplayName("README 예제 URL 형식 검증")
        void historyApi_UrlFormatMatchesReadme() throws Exception {
            // README: GET /api/v1/analysis/history?userId=user-001

            mockMvc.perform(get("/api/v1/analysis/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk());

            // 잘못된 URL은 404
            mockMvc.perform(get("/api/v1/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Context API - README Contract Validation")
    class ContextApiContractTest {

        @Test
        @DisplayName("POST /api/v1/context/save - README 예제 형식 검증")
        void contextSaveApi_WithReadmeExampleFormat() throws Exception {
            // given - README 예제와 동일한 형식
            String requestJson = """
                {
                  "userId": "user-001",
                  "category": "Prepared data category",
                  "data": {
                    "Prepared data Key 1": "Prepared data content 1",
                    "Prepared data Key 2": "Prepared data content 2"
                  }
                }
                """;

            // when & then
            mockMvc.perform(post("/api/v1/context/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value(200))
                    .andExpect(jsonPath("$.resultMsg").value("OK"))
                    .andExpect(jsonPath("$.context").exists())
                    .andExpect(jsonPath("$.context.userId").value("user-001"))
                    .andExpect(jsonPath("$.context.category").exists())
                    .andExpect(jsonPath("$.context.data").exists());
        }

        @Test
        @DisplayName("POST /api/v1/context/get - README 예제 형식 검증")
        void contextGetApi_WithReadmeExampleFormat() throws Exception {
            // given - 먼저 컨텍스트 저장
            String saveRequest = """
                {
                  "userId": "user-001",
                  "category": "test_category",
                  "data": {
                    "key1": "value1"
                  }
                }
                """;

            mockMvc.perform(post("/api/v1/context/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(saveRequest))
                    .andExpect(status().isOk());

            // when & then - README: POST /api/v1/context/get?userId=user-001
            mockMvc.perform(post("/api/v1/context/get")
                            .param("userId", "user-001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value(200))
                    .andExpect(jsonPath("$.resultMsg").value("OK"))
                    .andExpect(jsonPath("$.context").exists())
                    .andExpect(jsonPath("$.context.userId").value("user-001"))
                    .andExpect(jsonPath("$.context.category").exists())
                    .andExpect(jsonPath("$.context.data").exists());
        }

        @Test
        @DisplayName("POST /api/v1/context/update - README 예제 형식 검증")
        void contextUpdateApi_WithReadmeExampleFormat() throws Exception {
            // given - 먼저 컨텍스트 저장
            String saveRequest = """
                {
                  "userId": "user-001",
                  "category": "Prepared data category",
                  "data": {
                    "Prepared data Key 1": "Prepared data content 1"
                  }
                }
                """;

            mockMvc.perform(post("/api/v1/context/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(saveRequest))
                    .andExpect(status().isOk());

            // README 예제 업데이트 요청
            String updateRequest = """
                {
                  "userId": "user-001",
                  "category": "Prepared data category",
                  "data": {
                    "Prepared data Key 1": "Prepared data content 3",
                    "Prepared data Key 2": "Prepared data content 4"
                  }
                }
                """;

            // when & then
            mockMvc.perform(post("/api/v1/context/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequest))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value(200))
                    .andExpect(jsonPath("$.resultMsg").value("OK"))
                    .andExpect(jsonPath("$.context").exists())
                    .andExpect(jsonPath("$.context.data").exists());
        }

        @Test
        @DisplayName("POST /api/v1/context/delete - README 예제 형식 검증")
        void contextDeleteApi_WithReadmeExampleFormat() throws Exception {
            // given - 먼저 컨텍스트 저장
            String saveRequest = """
                {
                  "userId": "user-001",
                  "category": "test",
                  "data": {"key": "value"}
                }
                """;

            mockMvc.perform(post("/api/v1/context/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(saveRequest))
                    .andExpect(status().isOk());

            // when & then - README: POST /api/v1/context/delete?userId=user-001
            mockMvc.perform(post("/api/v1/context/delete")
                            .param("userId", "user-001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value(200))
                    .andExpect(jsonPath("$.resultMsg").value("OK"))
                    .andExpect(jsonPath("$.context").isEmpty());
        }

        @Test
        @DisplayName("모든 Context API가 ContextResponse 형식으로 응답")
        void contextApis_AllReturnContextResponseFormat() throws Exception {
            // README: "모든 API는 ContextResponse 형식으로 응답합니다"

            String saveRequest = """
                {
                  "userId": "user-001",
                  "category": "test",
                  "data": {"key": "value"}
                }
                """;

            // Save
            mockMvc.perform(post("/api/v1/context/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(saveRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultMsg").exists())
                    .andExpect(jsonPath("$.context").exists());

            // Get
            mockMvc.perform(post("/api/v1/context/get")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultMsg").exists())
                    .andExpect(jsonPath("$.context").exists());

            // Update
            mockMvc.perform(post("/api/v1/context/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(saveRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultMsg").exists())
                    .andExpect(jsonPath("$.context").exists());

            // Delete
            mockMvc.perform(post("/api/v1/context/delete")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultMsg").exists());
        }
    }

    @Nested
    @DisplayName("API Versioning Validation")
    class ApiVersioningTest {

        @Test
        @DisplayName("모든 API가 /api/v1 경로를 사용하는지 검증")
        void allApis_UseV1Prefix() throws Exception {
            // Analysis APIs
            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"user-001\",\"purpose\":\"test\",\"userPrompt\":[{\"dataKey\":\"k\",\"data\":{\"k\":\"v\"}}]}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/analysis/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk());

            // Context APIs
            mockMvc.perform(post("/api/v1/context/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"user-001\",\"category\":\"c\",\"data\":{\"k\":\"v\"}}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("잘못된 API 버전 사용 시 404 반환")
        void invalidApiVersion_Returns404() throws Exception {
            // v2는 존재하지 않음
            mockMvc.perform(get("/api/v2/analysis/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isNotFound());

            mockMvc.perform(post("/api/v2/context/save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Response Status Code Validation")
    class ResponseStatusCodeTest {

        @Test
        @DisplayName("성공 응답은 200 OK와 resultCode 200 반환")
        void successResponse_Returns200WithResultCode200() throws Exception {
            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("test")
                    .data(Map.of("key", "value"))
                    .build();

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("test")
                    .userPrompt(List.of(userPrompt))
                    .build();

            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())  // HTTP 200
                    .andExpect(jsonPath("$.resultCode").value(200))  // Response body resultCode
                    .andExpect(jsonPath("$.resultMsg").value("OK"));
        }

        @Test
        @DisplayName("잘못된 요청은 400 Bad Request 반환")
        void invalidRequest_Returns400() throws Exception {
            // Invalid JSON
            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isBadRequest());

            // Missing required parameter
            mockMvc.perform(get("/api/v1/analysis/history"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("지원하지 않는 HTTP 메서드는 405 반환")
        void unsupportedHttpMethod_Returns405() throws Exception {
            // POST /api/v1/analysis는 POST만 허용
            mockMvc.perform(get("/api/v1/analysis"))
                    .andExpect(status().isMethodNotAllowed());

            // GET /api/v1/analysis/history는 GET만 허용
            mockMvc.perform(post("/api/v1/analysis/history")
                            .param("userId", "user-001"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Field Structure Validation")
    class FieldStructureTest {

        @Test
        @DisplayName("InsightResponse의 issueCategories 배열 구조 검증")
        void insightResponse_IssueCategoriesStructure() throws Exception {
            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("test")
                    .data(Map.of("key", "value"))
                    .build();

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("test")
                    .userPrompt(List.of(userPrompt))
                    .build();

            // README 예제 구조: category, description, severity
            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.insight.issueCategories").isArray())
                    .andExpect(jsonPath("$.insight.issueCategories[0].category").exists())
                    .andExpect(jsonPath("$.insight.issueCategories[0].description").exists())
                    .andExpect(jsonPath("$.insight.issueCategories[0].severity").exists());
        }

        @Test
        @DisplayName("InsightResponse의 rootCauseInsights 배열 구조 검증")
        void insightResponse_RootCauseInsightsIsStringArray() throws Exception {
            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("test")
                    .data(Map.of("key", "value"))
                    .build();

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("test")
                    .userPrompt(List.of(userPrompt))
                    .build();

            mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.insight.rootCauseInsights").isArray());
        }

        @Test
        @DisplayName("InsightResponse의 priorityScore가 숫자형인지 검증")
        void insightResponse_PriorityScoreIsNumeric() throws Exception {
            UserPrompt userPrompt = UserPrompt.builder()
                    .dataKey("test")
                    .data(Map.of("key", "value"))
                    .build();

            InsightRequest request = InsightRequest.builder()
                    .userId("user-001")
                    .purpose("test")
                    .userPrompt(List.of(userPrompt))
                    .build();

            MvcResult result = mockMvc.perform(post("/api/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.insight.priorityScore").isNumber())
                    .andReturn();
        }
    }
}