package com.aiinsightagent.app.controller;

import com.aiinsightagent.app.TestApplication;
import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.aiinsightagent.core.queue.GeminiQueueManager;
import com.aiinsightagent.core.queue.GeminiResponse;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.errors.ClientException;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.repository.ActorRepository;
import com.aiinsightagent.app.repository.AnalysisResultRepository;
import com.aiinsightagent.app.repository.PreparedContextRepository;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional

@DisplayName("InsightController 통합 테스트")
class InsightControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ActorRepository actorRepository;

	@Autowired
	private PreparedContextRepository preparedContextRepository;

	@Autowired
	private AnalysisResultRepository analysisResultRepository;

	@MockitoBean
	private Client geminiClient;

	@MockitoBean
	private Models geminiModels;

	@MockitoBean
	private GeminiQueueManager geminiQueueManager;

	@MockitoBean
	private GeminiChatAdapter geminiChatAdapter;

	private Actor actor;
	private PreparedContext preparedContext;
	private InsightRequest insightRequest;
	private List<UserPrompt> userPrompts;

	@BeforeEach
	void setUp() {
		// Mock GeminiChatAdapter
		String mockJsonResponse = "{" +
			"\"summary\": \"Test analysis summary\"," +
			"\"issueCategories\": [{\"category\": \"Performance\", \"description\": \"Test issue\", \"severity\": \"MEDIUM\"}]," +
			"\"rootCauseInsights\": [\"Root cause 1\", \"Root cause 2\"]," +
			"\"recommendedActions\": [\"Action 1\", \"Action 2\"]," +
			"\"priorityScore\": 75" +
			"}";
		GenerateContentResponse mockResponse = org.mockito.Mockito.mock(GenerateContentResponse.class);
		when(mockResponse.text()).thenReturn(mockJsonResponse);
		GenerateContentResponseUsageMetadata mockUsage = org.mockito.Mockito.mock(GenerateContentResponseUsageMetadata.class);
		when(mockUsage.promptTokenCount()).thenReturn(Optional.of(100));
		when(mockUsage.candidatesTokenCount()).thenReturn(Optional.of(50));
		when(mockUsage.totalTokenCount()).thenReturn(Optional.of(150));
		when(mockResponse.usageMetadata()).thenReturn(Optional.of(mockUsage));
		GeminiResponse geminiResponse = new GeminiResponse(mockResponse, "m01", "gemini-2.5-flash");
		when(geminiChatAdapter.getResponse(anyString())).thenReturn(geminiResponse);

		// 테스트 데이터 초기화
		analysisResultRepository.deleteAll();
		preparedContextRepository.deleteAll();
		actorRepository.deleteAll();

		// Actor 생성 및 저장
		actor = Actor.create("test-user");
		actorRepository.save(actor);

		// PreparedContext 생성 및 저장
		Map<String, String> contextData = new HashMap<>();
		contextData.put("averagePace", "6:00");
		contextData.put("totalDistance", "100km");
		contextData.put("runningDays", "30");

		preparedContext = new PreparedContext(actor, "running_history", contextData.toString());
		preparedContextRepository.save(preparedContext);

		// UserPrompt 생성
		Map<String, String> promptData = new HashMap<>();
		promptData.put("query", "Analyze my running performance");
		promptData.put("focus", "pace_improvement");

		UserPrompt userPrompt = UserPrompt.builder()
				.dataKey("running_analysis")
				.data(promptData)
				.build();

		userPrompts = List.of(userPrompt);

		// InsightRequest 생성
		insightRequest = InsightRequest.builder()
				.userId("test-user")
				.purpose("performance_analysis")
				.userPrompt(userPrompts)
				.build();
	}

	@Nested
	@DisplayName("GET /api/v1/answer - Test Answer")
	class AnswerTest {

		@Test
		@DisplayName("성공: 단건 데이터 분석 요청")
		void answer_Success() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test_analysis")
							.param("prompt", "Test prompt for analysis"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insight").exists())
					.andExpect(jsonPath("$.resultMsg").exists());
		}

		@Test
		@DisplayName("성공: 다양한 purpose로 분석 요청")
		void answer_VariousPurposes_Success() throws Exception {
			// given
			String[] purposes = {
					"performance_analysis",
					"health_check",
					"training_recommendation",
					"injury_prevention"
			};

			// when & then
			for (String purpose : purposes) {
				mockMvc.perform(get("/api/v1/answer")
								.param("purpose", purpose)
								.param("prompt", "Test prompt for " + purpose))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.resultCode").value(200));
			}
		}

		@Test
		@DisplayName("실패: purpose 파라미터 누락")
		void answer_MissingPurpose_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("prompt", "Test prompt"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패: prompt 파라미터 누락")
		void answer_MissingPrompt_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test_analysis"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패: 모든 파라미터 누락")
		void answer_MissingAllParams_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/answer"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("성공: 긴 프롬프트로 요청")
		void answer_LongPrompt_Success() throws Exception {
			// given
			String longPrompt = "A".repeat(1000); // 1000자 길이의 프롬프트

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test_analysis")
							.param("prompt", longPrompt))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));
		}

		@Test
		@DisplayName("성공: 특수문자가 포함된 프롬프트")
		void answer_SpecialCharactersPrompt_Success() throws Exception {
			// given
			String specialPrompt = "Test @#$% & prompt with 특수문자 !?";

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test_analysis")
							.param("prompt", specialPrompt))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));
		}
	}

	@Nested
	@DisplayName("POST /api/v1/analysis - Data Analysis")
	class AnalysisTest {

		@Test
		@DisplayName("성공: 데이터 분석 요청")
		void analysis_Success() throws Exception {
			// given
			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insight").exists())
					.andExpect(jsonPath("$.resultMsg").exists());

			// DB 검증 - 분석 결과 저장 확인
			assertThat(analysisResultRepository.findAll()).isNotEmpty();
		}

		@Test
		@DisplayName("성공: 여러 UserPrompt 포함한 분석 요청")
		void analysis_WithMultiplePrompts_Success() throws Exception {
			// given - 여러 개의 UserPrompt 포함
			List<UserPrompt> multiplePrompts = List.of(
					UserPrompt.builder()
							.dataKey("pace_analysis")
							.data(Map.of("metric", "pace_trend", "period", "last_30_days"))
							.build(),
					UserPrompt.builder()
							.dataKey("training_recommendation")
							.data(Map.of("goal", "improve_endurance", "level", "intermediate"))
							.build(),
					UserPrompt.builder()
							.dataKey("injury_risk")
							.data(Map.of("assessment", "full_body", "history", "none"))
							.build()
			);

			InsightRequest requestWithMultiplePrompts = InsightRequest.builder()
					.userId("test-user")
					.purpose("comprehensive_analysis")
					.userPrompt(multiplePrompts)
					.build();

			String requestBody = objectMapper.writeValueAsString(requestWithMultiplePrompts);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));
		}

		@Test
		@DisplayName("성공: 여러 번 연속 분석 요청")
		void analysis_MultipleRequests_Success() throws Exception {
			// given
			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then - 3번 연속 요청
			for (int i = 0; i < 3; i++) {
				mockMvc.perform(post("/api/v1/analysis")
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.resultCode").value(200));
			}

			// DB 검증 - 3개의 분석 결과 저장 확인
			assertThat(analysisResultRepository.findAll()).hasSizeGreaterThanOrEqualTo(3);
		}

		@Test
		@DisplayName("실패: 잘못된 JSON 형식")
		void analysis_InvalidJson_Returns400() throws Exception {
			// given
			String invalidJson = "{invalid json}";

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(invalidJson))
					.andDo(print())
					.andExpect(status().isBadRequest());

			// DB 검증 - 저장되지 않음
			assertThat(analysisResultRepository.findAll()).isEmpty();
		}

		@Test
		@DisplayName("실패: Content-Type 누락")
		void analysis_NoContentType_Returns415() throws Exception {
			// given
			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isUnsupportedMediaType());
		}

		@Test
		@DisplayName("성공: 최소 필수 데이터만으로 분석 요청")
		void analysis_MinimalData_Success() throws Exception {
			// given - 최소 필수 데이터만 포함
			UserPrompt minimalPrompt = UserPrompt.builder()
					.dataKey("minimal")
					.data(Map.of("query", "basic analysis"))
					.build();

			InsightRequest minimalRequest = InsightRequest.builder()
					.userId("test-user")
					.purpose("minimal_analysis")
					.userPrompt(List.of(minimalPrompt))
					.build();

			String requestBody = objectMapper.writeValueAsString(minimalRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));
		}

		@Test
		@DisplayName("성공: 다양한 purpose로 분석 요청")
		void analysis_VariousPurposes_Success() throws Exception {
			// given
			String[] purposes = {
					"training_plan",
					"nutrition_advice",
					"recovery_analysis",
					"goal_setting"
			};

			for (String purpose : purposes) {
				UserPrompt prompt = UserPrompt.builder()
						.dataKey(purpose + "_key")
						.data(Map.of("request", "Test request for " + purpose))
						.build();

				InsightRequest request = InsightRequest.builder()
						.userId("test-user")
						.purpose(purpose)
						.userPrompt(List.of(prompt))
						.build();

				String requestBody = objectMapper.writeValueAsString(request);

				// when & then
				mockMvc.perform(post("/api/v1/analysis")
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.resultCode").value(200));
			}
		}
	}

	@Nested
	@DisplayName("GET /api/v1/analysis/history - Get History")
	class GetHistoryTest {

		@Test
		@DisplayName("성공: 분석 이력 조회")
		void getHistory_Success() throws Exception {
			// given - 먼저 분석 요청을 생성하여 이력 생성
			String requestBody = objectMapper.writeValueAsString(insightRequest);
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk());

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insightRecords").isArray());
		}

		@Test
		@DisplayName("성공: 여러 분석 이력 조회")
		void getHistory_MultipleAnalysis_Success() throws Exception {
			// given - 3개의 분석 요청 생성
			for (int i = 0; i < 3; i++) {
				UserPrompt prompt = UserPrompt.builder()
						.dataKey("analysis_" + i)
						.data(Map.of("request", "Prompt " + i))
						.build();

				InsightRequest request = InsightRequest.builder()
						.userId("test-user")
						.purpose("analysis_" + i)
						.userPrompt(List.of(prompt))
						.build();

				mockMvc.perform(post("/api/v1/analysis")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk());
			}

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insightRecords").isArray())
					.andExpect(jsonPath("$.insightRecords.length()").value(3));
		}

		@Test
		@DisplayName("성공: 분석 이력이 없는 사용자 조회")
		void getHistory_NoHistory_Success() throws Exception {
			// given - 새로운 사용자 (이력 없음)
			Actor newActor = Actor.create("new-user");
			actorRepository.save(newActor);

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "new-user"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insightRecords").isArray())
					.andExpect(jsonPath("$.insightRecords").isEmpty());
		}

		@Test
		@DisplayName("실패: userId 파라미터 누락")
		void getHistory_MissingUserId_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/analysis/history"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 조회")
		void getHistory_NonExistentUser_ThrowsException() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "non-existent-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("성공: 특정 사용자만의 이력 조회 (다른 사용자 이력 분리)")
		void getHistory_OnlySpecificUserHistory_Success() throws Exception {
			// given - 여러 사용자의 분석 요청
			String[] userIds = {"user1", "user2", "user3"};

			for (String userId : userIds) {
				Actor actor = Actor.create(userId);
				actorRepository.save(actor);

				for (int i = 0; i < 2; i++) {
					UserPrompt prompt = UserPrompt.builder()
							.dataKey("analysis_" + i)
							.data(Map.of("request", "Request " + i + " for " + userId))
							.build();

					InsightRequest request = InsightRequest.builder()
							.userId(userId)
							.purpose("analysis_" + userId)
							.userPrompt(List.of(prompt))
							.build();

					mockMvc.perform(post("/api/v1/analysis")
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(request)))
							.andExpect(status().isOk());
				}
			}

			// when & then - user1의 이력만 조회
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "user1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insightRecords.length()").value(2));

			// user2의 이력 조회
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "user2"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insightRecords.length()").value(2));
		}
	}

	@Nested
	@DisplayName("전체 워크플로우 통합 시나리오")
	class IntegrationScenarioTest {

		@Test
		@DisplayName("시나리오: 분석 요청 -> 이력 조회")
		void analysisAndHistory_Workflow() throws Exception {
			// 1. 첫 번째 분석 요청
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(insightRequest)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));

			// 2. 이력 조회 - 1개 존재
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.insightRecords.length()").value(1));

			// 3. 두 번째 분석 요청
			UserPrompt secondPrompt = UserPrompt.builder()
					.dataKey("second_analysis")
					.data(Map.of("request", "Second analysis request"))
					.build();

			InsightRequest secondRequest = InsightRequest.builder()
					.userId("test-user")
					.purpose("second_analysis")
					.userPrompt(List.of(secondPrompt))
					.build();

			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(secondRequest)))
					.andExpect(status().isOk());

			// 4. 이력 조회 - 2개 존재
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.insightRecords.length()").value(2));
		}

		@Test
		@DisplayName("시나리오: 테스트 분석 -> 정식 분석 -> 이력 조회")
		void testAnalysisToFullAnalysis_Workflow() throws Exception {
			// 1. 테스트 분석 (answer 엔드포인트)
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "Test analysis"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));

			// 2. 정식 분석 요청
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(insightRequest)))
					.andExpect(status().isOk());

			// 3. 이력 조회 (정식 분석만 이력에 포함)
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.insightRecords").isArray());
		}

		@Test
		@DisplayName("시나리오: 여러 사용자의 독립적인 분석 및 이력")
		void multipleUsersIndependentAnalysis() throws Exception {
			// given - 3명의 사용자
			String[] userIds = {"athlete1", "athlete2", "athlete3"};

			for (String userId : userIds) {
				Actor actor = Actor.create(userId);
				actorRepository.save(actor);
			}

			// when - 각 사용자별 분석 요청 (횟수 다르게)
			for (int i = 0; i < userIds.length; i++) {
				for (int j = 0; j <= i; j++) { // athlete1: 1회, athlete2: 2회, athlete3: 3회
					UserPrompt prompt = UserPrompt.builder()
							.dataKey("analysis_" + j)
							.data(Map.of("index", String.valueOf(j), "user", userIds[i]))
							.build();

					InsightRequest request = InsightRequest.builder()
							.userId(userIds[i])
							.purpose("analysis_" + j)
							.userPrompt(List.of(prompt))
							.build();

					mockMvc.perform(post("/api/v1/analysis")
									.contentType(MediaType.APPLICATION_JSON)
									.content(objectMapper.writeValueAsString(request)))
							.andExpect(status().isOk());
				}
			}

			// then - 각 사용자별 이력 개별 확인
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "athlete1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.insightRecords.length()").value(1));

			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "athlete2"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.insightRecords.length()").value(2));

			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "athlete3"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.insightRecords.length()").value(3));
		}

		@Test
		@DisplayName("시나리오: 다양한 purpose별 분석 및 통합 이력")
		void variousPurposesAnalysis() throws Exception {
			// given
			String[] purposes = {
					"performance_analysis",
					"health_check",
					"training_plan",
					"recovery_status",
					"goal_review"
			};

			// when - 다양한 purpose로 분석 요청
			for (String purpose : purposes) {
				UserPrompt prompt = UserPrompt.builder()
						.dataKey(purpose + "_data")
						.data(Map.of("type", purpose, "action", "analyze"))
						.build();

				InsightRequest request = InsightRequest.builder()
						.userId("test-user")
						.purpose(purpose)
						.userPrompt(List.of(prompt))
						.build();

				mockMvc.perform(post("/api/v1/analysis")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.resultCode").value(200));
			}

			// then - 모든 purpose의 이력 조회
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.insightRecords.length()").value(purposes.length));
		}
	}

	@Nested
	@DisplayName("API 엔드포인트 및 HTTP 메서드 검증")
	class ApiMappingTest {

		@Test
		@DisplayName("GET /api/v1/answer는 GET만 허용")
		void answerEndpoint_OnlyGetAllowed() throws Exception {
			// GET - 성공
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andExpect(status().isOk());

			// POST - 405
			mockMvc.perform(post("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andExpect(status().isMethodNotAllowed());
		}

		@Test
		@DisplayName("POST /api/v1/analysis는 POST만 허용")
		void analysisEndpoint_OnlyPostAllowed() throws Exception {
			// POST - 성공
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(insightRequest)))
					.andExpect(status().isOk());

			// GET - 405
			mockMvc.perform(get("/api/v1/analysis"))
					.andExpect(status().isMethodNotAllowed());
		}

		@Test
		@DisplayName("GET /api/v1/analysis/history는 GET만 허용")
		void historyEndpoint_OnlyGetAllowed() throws Exception {
			// GET - 성공 (또는 적절한 응답)
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk());

			// POST - 405
			mockMvc.perform(post("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isMethodNotAllowed());
		}

		@Test
		@DisplayName("잘못된 경로 접근 시 404")
		void wrongPath_Returns404() throws Exception {
			mockMvc.perform(get("/api/v1/invalid"))
					.andExpect(status().isNotFound());

			mockMvc.perform(post("/api/v1/invalid")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("기본 경로 검증")
		void basePathVerification() throws Exception {
			// 정상 경로
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andExpect(status().isOk());

			// 잘못된 버전
			mockMvc.perform(get("/api/v2/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("에러 처리 및 예외 상황")
	class ErrorHandlingTest {

		@Test
		@DisplayName("빈 문자열 파라미터로 요청")
		void emptyParameters_HandledGracefully() throws Exception {
			// answer 엔드포인트
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "")
							.param("prompt", ""))
					.andDo(print());
		}

		@Test
		@DisplayName("null 값이 포함된 InsightRequest")
		void nullValuesInRequest_HandledGracefully() throws Exception {
			// given
			InsightRequest requestWithNulls = InsightRequest.builder()
					.userId("test-user")
					.purpose(null)
					.userPrompt(null)
					.build();

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(requestWithNulls)))
					.andDo(print());
		}

		@Test
		@DisplayName("매우 긴 데이터로 요청")
		void veryLongData_HandledGracefully() throws Exception {
			// given
			String longValue = "A".repeat(10000);
			UserPrompt longPrompt = UserPrompt.builder()
					.dataKey("long_data_test")
					.data(Map.of("longField", longValue))
					.build();

			InsightRequest longRequest = InsightRequest.builder()
					.userId("test-user")
					.purpose("test")
					.userPrompt(List.of(longPrompt))
					.build();

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(longRequest)))
					.andDo(print());
		}

		@Test
		@DisplayName("빈 data Map으로 요청")
		void emptyDataMap_HandledGracefully() throws Exception {
			// given
			UserPrompt emptyDataPrompt = UserPrompt.builder()
					.dataKey("empty_test")
					.data(new HashMap<>())
					.build();

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("test")
					.userPrompt(List.of(emptyDataPrompt))
					.build();

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print());
		}

		@Test
		@DisplayName("dataKey만 있고 data가 null인 경우")
		void nullDataField_HandledGracefully() throws Exception {
			// given
			UserPrompt nullDataPrompt = UserPrompt.builder()
					.dataKey("test_key")
					.data(null)
					.build();

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("test")
					.userPrompt(List.of(nullDataPrompt))
					.build();

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print());
		}
	}

	@Nested
	@DisplayName("429 Too Many Requests - Rate Limit 테스트")
	class RateLimitTest {

		@Test
		@DisplayName("실패: Gemini API Rate Limit 초과 시 429 반환 (POST /api/v1/analysis)")
		void analysis_RateLimitExceeded_Returns429() throws Exception {
			// given
			when(geminiChatAdapter.getResponse(anyString()))
					.thenThrow(new ClientException(429, "Resource has been exhausted", "RATE_LIMIT_EXCEEDED"));

			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isTooManyRequests())
					.andExpect(jsonPath("$.code").value(429))
					.andExpect(jsonPath("$.message").exists())
					.andExpect(jsonPath("$.path").value("/api/v1/analysis"));
		}

		@Test
		@DisplayName("실패: Gemini API Rate Limit 초과 시 429 반환 (GET /api/v1/answer)")
		void answer_RateLimitExceeded_Returns429() throws Exception {
			// given
			when(geminiChatAdapter.getResponse(anyString()))
					.thenThrow(new ClientException(429, "Resource has been exhausted", "RATE_LIMIT_EXCEEDED"));

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test_analysis")
							.param("prompt", "Test prompt"))
					.andDo(print())
					.andExpect(status().isTooManyRequests())
					.andExpect(jsonPath("$.code").value(429))
					.andExpect(jsonPath("$.message").exists())
					.andExpect(jsonPath("$.path").value("/api/v1/answer"));
		}

		@Test
		@DisplayName("실패: 연속 요청 시 Rate Limit 발생 시나리오")
		void analysis_ConsecutiveRequests_RateLimitOccurs() throws Exception {
			// given - 첫 번째 요청은 성공, 두 번째부터 Rate Limit
			String mockJsonResponse = "{" +
					"\"summary\": \"Test analysis summary\"," +
					"\"issueCategories\": []," +
					"\"rootCauseInsights\": []," +
					"\"recommendedActions\": []," +
					"\"priorityScore\": 50" +
					"}";
			GenerateContentResponse mockResponse = org.mockito.Mockito.mock(GenerateContentResponse.class);
			when(mockResponse.text()).thenReturn(mockJsonResponse);
			GenerateContentResponseUsageMetadata mockUsage = org.mockito.Mockito.mock(GenerateContentResponseUsageMetadata.class);
			when(mockUsage.promptTokenCount()).thenReturn(Optional.of(100));
			when(mockUsage.candidatesTokenCount()).thenReturn(Optional.of(50));
			when(mockUsage.totalTokenCount()).thenReturn(Optional.of(150));
			when(mockResponse.usageMetadata()).thenReturn(Optional.of(mockUsage));
			GeminiResponse geminiResp = new GeminiResponse(mockResponse, "m01", "gemini-2.5-flash");

			when(geminiChatAdapter.getResponse(anyString()))
					.thenReturn(geminiResp)  // 첫 번째 호출: 성공
					.thenThrow(new ClientException(429, "Resource has been exhausted", "RATE_LIMIT_EXCEEDED"));  // 두 번째 호출: Rate Limit

			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then - 첫 번째 요청 성공
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));

			// when & then - 두 번째 요청 Rate Limit
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andExpect(status().isTooManyRequests())
					.andExpect(jsonPath("$.code").value(429));
		}

		@Test
		@DisplayName("실패: Rate Limit 응답 메시지 검증")
		void analysis_RateLimitMessage_ContainsDetails() throws Exception {
			// given
			String rateLimitMessage = "Quota exceeded for quota metric 'Generate Content API requests per minute'";
			when(geminiChatAdapter.getResponse(anyString()))
					.thenThrow(new ClientException(429, rateLimitMessage, "RATE_LIMIT_EXCEEDED"));

			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isTooManyRequests())
					.andExpect(jsonPath("$.code").value(429))
					.andExpect(jsonPath("$.message").isNotEmpty());
		}
	}
}
