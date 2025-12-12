package com.aiinsightagent.app.controller;

import com.aiinsightagent.core.facade.InsightFacade;
import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InsightController.class)
class InsightControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private InsightFacade insightFacade;

	@Test
	@DisplayName("GET /api/v1/answer - 성공")
	void testAnswer() throws Exception {
		// given
		InsightDetail detail = InsightDetail.builder()
				.summary("요약 내용")
				.issueCategories(Collections.emptyList())
				.rootCauseInsights(Collections.emptyList())
				.recommendedActions(Collections.emptyList())
				.priorityScore(5)
				.build();

		InsightResponse mockResponse = InsightResponse.builder()
				.resultCode(200).resultMsg("SUCCESS")
				.insight(detail)
				.build();

		when(insightFacade.answer(eq("purpose"), eq("prompt"))).thenReturn(mockResponse);

		// when & then
		mockMvc.perform(get("/api/v1/answer")
						.param("purpose", "purpose")
						.param("prompt", "prompt"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value(200))
				.andExpect(jsonPath("$.resultMsg").value("SUCCESS"))
				.andExpect(jsonPath("$.insight.summary").value("요약 내용"))
				.andExpect(jsonPath("$.insight.priorityScore").value(5));
	}

	@Test
	@DisplayName("POST /api/v1/analysis - 성공")
	void testAnalysis() throws Exception {
		// given
		InsightRequest request = InsightRequest.builder()
				.purpose("분석 목적")
				.userPrompt(Collections.emptyList())
				.build();

		InsightDetail detail = InsightDetail.builder()
				.summary("분석 요약")
				.issueCategories(Collections.emptyList())
				.rootCauseInsights(Collections.emptyList())
				.recommendedActions(Collections.emptyList())
				.priorityScore(3)
				.build();

		InsightResponse mockResponse = InsightResponse.builder()
				.resultCode(200)
				.resultMsg("SUCCESS")
				.insight(detail)
				.build();

		when(insightFacade.analysis(any(InsightRequest.class))).thenReturn(mockResponse);

		// when & then
		mockMvc.perform(post("/api/v1/analysis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value(200))
				.andExpect(jsonPath("$.resultMsg").value("SUCCESS"))
				.andExpect(jsonPath("$.insight.summary").value("분석 요약"))
				.andExpect(jsonPath("$.insight.priorityScore").value(3));
	}

	@Test
	@DisplayName("GET /api/v1/answer - InsightFacade 예외 발생 시 500 응답")
	void testAnswerFailureException() throws Exception {

		when(insightFacade.answer(eq("purpose"), eq("prompt")))
				.thenThrow(new RuntimeException("AI 오류"));

		mockMvc.perform(get("/api/v1/answer")
						.param("purpose", "purpose")
						.param("prompt", "prompt"))
				.andExpect(status().is5xxServerError());
	}

	@Test
	@DisplayName("GET /api/v1/answer - 필수 파라미터 누락 시 400 응답")
	void testAnswerFailureMissingParam() throws Exception {

		mockMvc.perform(get("/api/v1/answer")
						.param("purpose", "purpose"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/v1/analysis - invalid JSON → 400 응답")
	void testAnalysisFailureInvalidJson() throws Exception {

		String invalidJson = "{ \"purpose\": \"test\", ";  // JSON 문법 오류

		mockMvc.perform(post("/api/v1/analysis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/v1/analysis - InsightFacade 예외 발생 시 500 응답")
	void testAnalysisFailureException() throws Exception {

		InsightRequest request = InsightRequest.builder()
				.purpose("test")
				.userPrompt(Collections.emptyList())
				.build();

		when(insightFacade.analysis(any()))
				.thenThrow(new RuntimeException("AI 처리 실패"));

		mockMvc.perform(post("/api/v1/analysis")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().is5xxServerError());
	}
}