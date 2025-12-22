package com.aiinsightagent.core.parser;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightDetail;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.preprocess.LlmJsonPreprocessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class GeminiResponseParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private GeminiResponseParser() {
	}

	public static InsightResponse toInsightResponse(GenerateContentResponse response) {
		String raw = response.text();
		String pureJson = LlmJsonPreprocessor.extractPureJson(raw);
		if (pureJson == null || pureJson.isBlank()) {
			throw new InsightException(InsightError.EMPTY_GEMINI_RESPONSE);
		}

		InsightDetail insightDetail = null;
		try {
			insightDetail = objectMapper.readValue(pureJson, InsightDetail.class);
		} catch (IOException e) {
			throw new InsightException(InsightError.FAIL_JSON_PARSING, e);
		}

		return InsightResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insight(insightDetail)
				.build();
	}
}