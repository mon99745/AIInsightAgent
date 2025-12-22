package com.aiinsightagent.core.parser;

import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.preprocess.LlmJsonPreprocessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class GeminiResponseParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private GeminiResponseParser() {
	}

	public static InsightResponse toInsightResponse(GenerateContentResponse response) {
		String raw = response.text();
		String pureJson = LlmJsonPreprocessor.extractPureJson(raw);
		if (pureJson == null || pureJson.isBlank()) {
			throw new RuntimeException("Gemini returned empty response");
		}

		InsightDetail insightDetail = null;
		try {
			insightDetail = objectMapper.readValue(pureJson, InsightDetail.class);
		} catch (Exception e) {
			log.error("Failed to parse JSON. raw: {}", raw);
			throw new RuntimeException("Failed to parse Gemini response to InsightDetail", e);
		}

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