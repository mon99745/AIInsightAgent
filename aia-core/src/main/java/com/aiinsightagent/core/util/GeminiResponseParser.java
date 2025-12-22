package com.aiinsightagent.core.util;

import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.http.HttpStatus;

public class GeminiResponseParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private GeminiResponseParser() {}

	public static InsightResponse toInsightResponse(GenerateContentResponse response) {

		String rawText = response.text();
		if (rawText == null || rawText.isBlank()) {
			throw new IllegalStateException("Gemini response text is empty");
		}

		// 1. 개행 정리
		String normalized = formatNewLines(rawText);

		// 2. JSON → 객체 파싱
		InsightDetail insightDetail;
		try {
			insightDetail = objectMapper.readValue(normalized, InsightDetail.class);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to parse Gemini response to InsightDetail. raw=" + rawText, e
			);
		}

		// 3. 응답 래핑
		return InsightResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insight(insightDetail)
				.build();
	}

	public static String formatNewLines(String text) {
		if (text == null) return null;
		return text.replace("\\n", "\n");
	}
}