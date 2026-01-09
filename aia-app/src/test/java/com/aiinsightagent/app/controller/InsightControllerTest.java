package com.aiinsightagent.app.controller;

import com.aiinsightagent.app.service.InsightService;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightHistoryResponse;
import com.aiinsightagent.core.model.InsightRecord;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InsightController.class)
@ActiveProfiles("test")
@DisplayName("InsightController 테스트")
class InsightControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private InsightService insightService;

	private InsightRequest insightRequest;
	private InsightResponse insightResponse;
	private InsightHistoryResponse historyResponse;
	private List<UserPrompt> userPrompts;

	@BeforeEach
	void setUp() {
		// UserPrompt 리스트 생성
		userPrompts = createUserPrompts();

		// InsightRequest 생성
		insightRequest = InsightRequest.builder()
				.userId("test-user")
				.purpose("running_style_analysis")
				.userPrompt(userPrompts)
				.build();

		// InsightResponse 생성
		InsightDetail insightDetail = mock(InsightDetail.class);
		insightResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("Success")
				.insight(insightDetail)
				.build();

		// InsightHistoryResponse 생성
		historyResponse = InsightHistoryResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insightRecords(Collections.emptyList())
				.build();
	}

	private List<UserPrompt> createUserPrompts() {
		Map<String, String> data1 = new HashMap<>();
		data1.put("duration", "3556");
		data1.put("heartRate", "194.63");
		data1.put("distance", "9.95");
		data1.put("stepCount", "10114");

		Map<String, String> data2 = new HashMap<>();
		data2.put("duration", "1965");
		data2.put("heartRate", "181.92");
		data2.put("distance", "6.01");
		data2.put("stepCount", "5702");

		return Arrays.asList(
				UserPrompt.builder()
						.dataKey("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99")
						.data(data1)
						.build(),
				UserPrompt.builder()
						.dataKey("2FEC0793-820B-4F82-BBBA-951FB26B7455")
						.data(data2)
						.build()
		);
	}

	@Nested
	@DisplayName("GET /api/v1/answer")
	class AnswerTest {

		@Test
		@DisplayName("단건 데이터 분석 요청 성공")
		void answer_Success() throws Exception {
			// given
			String purpose = "running_style_analysis";
			String prompt = "내 러닝 스타일을 분석해줘";

			given(insightService.requestInsight(purpose, prompt))
					.willReturn(insightResponse);

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", purpose)
							.param("prompt", prompt))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.resultMsg").value("Success"))
					.andExpect(jsonPath("$.insight").exists());

			verify(insightService, times(1)).requestInsight(purpose, prompt);
		}

		@Test
		@DisplayName("다양한 purpose와 prompt로 요청")
		void answer_VariousPurposes_Success() throws Exception {
			// given
			String purpose = "health_recommendation";
			String prompt = "건강 상태를 분석해줘";

			given(insightService.requestInsight(purpose, prompt))
					.willReturn(insightResponse);

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", purpose)
							.param("prompt", prompt))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));

			verify(insightService, times(1)).requestInsight(purpose, prompt);
		}

		@Test
		@DisplayName("purpose 파라미터 누락 시 400 에러")
		void answer_MissingPurpose_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("prompt", "test prompt"))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(insightService);
		}

		@Test
		@DisplayName("prompt 파라미터 누락 시 400 에러")
		void answer_MissingPrompt_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test purpose"))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(insightService);
		}

		@Test
		@DisplayName("모든 파라미터 누락 시 400 에러")
		void answer_MissingAllParams_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/answer"))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(insightService);
		}

		@Test
		@DisplayName("빈 문자열 파라미터로 요청")
		void answer_EmptyParams_Success() throws Exception {
			// given
			given(insightService.requestInsight("", ""))
					.willReturn(insightResponse);

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "")
							.param("prompt", ""))
					.andDo(print())
					.andExpect(status().isOk());

			verify(insightService, times(1)).requestInsight("", "");
		}

		@Test
		@DisplayName("Service에서 예외 발생 시 500 에러")
		void answer_ServiceException_Returns500() throws Exception {
			// given
			given(insightService.requestInsight(anyString(), anyString()))
					.willThrow(new RuntimeException("Service error"));

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(insightService, times(1)).requestInsight(anyString(), anyString());
		}

		@Test
		@DisplayName("한글 파라미터 처리")
		void answer_KoreanParams_Success() throws Exception {
			// given
			String purpose = "러닝_스타일_분석";
			String prompt = "내 러닝 스타일을 분석해주세요";

			given(insightService.requestInsight(purpose, prompt))
					.willReturn(insightResponse);

			// when & then
			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", purpose)
							.param("prompt", prompt)
							.characterEncoding("UTF-8"))
					.andDo(print())
					.andExpect(status().isOk());

			verify(insightService, times(1)).requestInsight(purpose, prompt);
		}
	}

	@Nested
	@DisplayName("POST /api/v1/analysis")
	class AnalysisTest {

		@Test
		@DisplayName("데이터 분석 요청 성공")
		void analysis_Success() throws Exception {
			// given
			given(insightService.requestInsight(any(InsightRequest.class)))
					.willReturn(insightResponse);

			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.resultMsg").value("Success"))
					.andExpect(jsonPath("$.insight").exists());

			verify(insightService, times(1)).requestInsight(any(InsightRequest.class));
		}

		@Test
		@DisplayName("여러 UserPrompt를 포함한 분석 요청")
		void analysis_MultipleUserPrompts_Success() throws Exception {
			// given
			List<UserPrompt> multiplePrompts = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				Map<String, String> data = new HashMap<>();
				data.put("duration", String.valueOf(3000 + i * 100));
				data.put("heartRate", String.valueOf(180 + i));
				data.put("distance", String.valueOf(10.0 - i * 0.5));
				data.put("stepCount", String.valueOf(10000 + i * 100));

				multiplePrompts.add(UserPrompt.builder()
						.dataKey("KEY-" + i)
						.data(data)
						.build());
			}

			InsightRequest multiRequest = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(multiplePrompts)
					.build();

			given(insightService.requestInsight(any(InsightRequest.class)))
					.willReturn(insightResponse);

			String requestBody = objectMapper.writeValueAsString(multiRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200));

			verify(insightService, times(1)).requestInsight(any(InsightRequest.class));
		}

		@Test
		@DisplayName("잘못된 JSON 형식으로 요청 시 400 에러")
		void analysis_InvalidJson_Returns400() throws Exception {
			// given
			String invalidJson = "{invalid json}";

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(invalidJson))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(insightService);
		}

		@Test
		@DisplayName("Content-Type 누락 시 415 에러")
		void analysis_NoContentType_Returns415() throws Exception {
			// given
			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isUnsupportedMediaType());

			verifyNoInteractions(insightService);
		}

		@Test
		@DisplayName("빈 요청 본문으로 요청")
		void analysis_EmptyBody_Returns400() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(""))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(insightService);
		}

		@Test
		@DisplayName("Service에서 검증 예외 발생")
		void analysis_ValidationException_Returns500() throws Exception {
			// given
			given(insightService.requestInsight(any(InsightRequest.class)))
					.willThrow(new IllegalArgumentException("Validation failed"));

			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(insightService, times(1)).requestInsight(any(InsightRequest.class));
		}

		@Test
		@DisplayName("InsightException 발생 시 500 에러")
		void analysis_InsightException_Returns500() throws Exception {
			// given
			given(insightService.requestInsight(any(InsightRequest.class)))
					.willThrow(new InsightException(InsightError.EMPTY_USER_PROMPT));

			String requestBody = objectMapper.writeValueAsString(insightRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(insightService, times(1)).requestInsight(any(InsightRequest.class));
		}

		@Test
		@DisplayName("다양한 purpose로 분석 요청")
		void analysis_DifferentPurposes_Success() throws Exception {
			// given
			InsightRequest healthRequest = InsightRequest.builder()
					.userId("test-user")
					.purpose("health_analysis")
					.userPrompt(userPrompts)
					.build();

			given(insightService.requestInsight(any(InsightRequest.class)))
					.willReturn(insightResponse);

			String requestBody = objectMapper.writeValueAsString(healthRequest);

			// when & then
			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
					.andDo(print())
					.andExpect(status().isOk());

			verify(insightService, times(1)).requestInsight(any(InsightRequest.class));
		}
	}

	@Nested
	@DisplayName("GET /api/v1/analysis/history")
	class GetHistoryTest {

		@Test
		@DisplayName("사용자 히스토리 조회 성공")
		void getHistory_Success() throws Exception {
			// given
			given(insightService.getHistory("test-user"))
					.willReturn(historyResponse);

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.resultCode").value(200))
					.andExpect(jsonPath("$.resultMsg").value("OK"))
					.andExpect(jsonPath("$.insightRecords").isArray());

			verify(insightService, times(1)).getHistory("test-user");
		}

		@Test
		@DisplayName("히스토리가 있는 경우 레코드 반환")
		void getHistory_WithRecords_Success() throws Exception {
			// given
			List<InsightRecord> records = Arrays.asList(
					InsightRecord.builder()
							.inputId(1L)
							.userPrompt(userPrompts.get(0))
							.build(),
					InsightRecord.builder()
							.inputId(2L)
							.userPrompt(userPrompts.get(1))
							.build()
			);

			InsightHistoryResponse responseWithRecords = InsightHistoryResponse.builder()
					.resultCode(200)
					.resultMsg("OK")
					.insightRecords(records)
					.build();

			given(insightService.getHistory("test-user"))
					.willReturn(responseWithRecords);

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.insightRecords").isArray())
					.andExpect(jsonPath("$.insightRecords.length()").value(2))
					.andExpect(jsonPath("$.insightRecords[0].inputId").value(1))
					.andExpect(jsonPath("$.insightRecords[1].inputId").value(2));

			verify(insightService, times(1)).getHistory("test-user");
		}

		@Test
		@DisplayName("존재하지 않는 사용자 히스토리 조회 시 예외 발생")
		void getHistory_UserNotFound_ThrowsException() throws Exception {
			// given
			given(insightService.getHistory("non-existent-user"))
					.willThrow(new InsightException(InsightError.NOT_FOUND_ACTOR + ":non-existent-user"));

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "non-existent-user"))
					.andDo(print())
					.andExpect(status().is5xxServerError());

			verify(insightService, times(1)).getHistory("non-existent-user");
		}

		@Test
		@DisplayName("userId 파라미터 누락 시 400 에러")
		void getHistory_MissingUserId_Returns400() throws Exception {
			// when & then
			mockMvc.perform(get("/api/v1/analysis/history"))
					.andDo(print())
					.andExpect(status().isBadRequest());

			verifyNoInteractions(insightService);
		}

		@Test
		@DisplayName("빈 userId로 요청")
		void getHistory_EmptyUserId_Success() throws Exception {
			// given
			given(insightService.getHistory(""))
					.willReturn(historyResponse);

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", ""))
					.andDo(print())
					.andExpect(status().isOk());

			verify(insightService, times(1)).getHistory("");
		}

		@Test
		@DisplayName("여러 사용자의 히스토리 순차 조회")
		void getHistory_MultipleUsers_Success() throws Exception {
			// given
			given(insightService.getHistory(anyString()))
					.willReturn(historyResponse);

			// when & then
			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "user-1"))
					.andExpect(status().isOk());

			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "user-2"))
					.andExpect(status().isOk());

			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "user-3"))
					.andExpect(status().isOk());

			verify(insightService, times(3)).getHistory(anyString());
		}
	}

	@Nested
	@DisplayName("API 경로 및 메서드 검증")
	class ApiMappingTest {

		@Test
		@DisplayName("GET /api/v1/answer 경로 확인")
		void answerPath() throws Exception {
			given(insightService.requestInsight(anyString(), anyString()))
					.willReturn(insightResponse);

			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andExpect(status().isOk());
		}

		@Test
		@DisplayName("POST /api/v1/analysis 경로 확인")
		void analysisPath() throws Exception {
			given(insightService.requestInsight(any(InsightRequest.class)))
					.willReturn(insightResponse);

			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(insightRequest)))
					.andExpect(status().isOk());
		}

		@Test
		@DisplayName("GET /api/v1/analysis/history 경로 확인")
		void historyPath() throws Exception {
			given(insightService.getHistory(anyString()))
					.willReturn(historyResponse);

			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test"))
					.andExpect(status().isOk());
		}

		@Test
		@DisplayName("잘못된 경로 접근 시 404")
		void wrongPath_Returns404() throws Exception {
			mockMvc.perform(get("/api/v1/invalid"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("POST로 answer 호출 시 405")
		void wrongMethodForAnswer_Returns405() throws Exception {
			mockMvc.perform(post("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andExpect(status().isMethodNotAllowed());
		}

		@Test
		@DisplayName("GET으로 analysis 호출 시 405")
		void wrongMethodForAnalysis_Returns405() throws Exception {
			mockMvc.perform(get("/api/v1/analysis"))
					.andExpect(status().isMethodNotAllowed());
		}

		@Test
		@DisplayName("POST로 history 호출 시 405")
		void wrongMethodForHistory_Returns405() throws Exception {
			mockMvc.perform(post("/api/v1/analysis/history")
							.param("userId", "test"))
					.andExpect(status().isMethodNotAllowed());
		}
	}

	@Nested
	@DisplayName("통합 시나리오 테스트")
	class IntegrationScenarioTest {

		@Test
		@DisplayName("분석 요청 -> 히스토리 조회 흐름")
		void analysisAndHistoryFlow() throws Exception {
			// 1. Analysis 요청
			given(insightService.requestInsight(any(InsightRequest.class)))
					.willReturn(insightResponse);

			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(insightRequest)))
					.andExpect(status().isOk());

			// 2. History 조회
			given(insightService.getHistory("test-user"))
					.willReturn(historyResponse);

			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk());

			// Verify
			verify(insightService, times(1)).requestInsight(any(InsightRequest.class));
			verify(insightService, times(1)).getHistory("test-user");
		}

		@Test
		@DisplayName("Answer -> Analysis -> History 전체 흐름")
		void fullWorkflow() throws Exception {
			// 1. Answer 테스트
			given(insightService.requestInsight(anyString(), anyString()))
					.willReturn(insightResponse);

			mockMvc.perform(get("/api/v1/answer")
							.param("purpose", "test")
							.param("prompt", "test"))
					.andExpect(status().isOk());

			// 2. Analysis 요청
			given(insightService.requestInsight(any(InsightRequest.class)))
					.willReturn(insightResponse);

			mockMvc.perform(post("/api/v1/analysis")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(insightRequest)))
					.andExpect(status().isOk());

			// 3. History 조회
			given(insightService.getHistory("test-user"))
					.willReturn(historyResponse);

			mockMvc.perform(get("/api/v1/analysis/history")
							.param("userId", "test-user"))
					.andExpect(status().isOk());

			// Verify
			verify(insightService, times(1)).requestInsight(anyString(), anyString());
			verify(insightService, times(1)).requestInsight(any(InsightRequest.class));
			verify(insightService, times(1)).getHistory("test-user");
		}
	}
}