package com.aiinsightagent.core.util;

import com.aiinsightagent.common.message.InsightResponse;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.http.HttpStatus;

public class GeminiResponseParser {
	private GeminiResponseParser() {}

	public static InsightResponse toInsightResponse(GenerateContentResponse response) {
		String insightText = response.text();

		return InsightResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insight(insightText)
				.build();
	}

	public static String formatNewLines(String text) {
		if (text == null) return null;
		return text.replace("\\n", "\n");
	}
}