package com.aiinsightagent.core.preprocess;

public class LlmJsonPreprocessor {

	private LlmJsonPreprocessor() {
	}

	/**
	 * LLM이 반환한 응답에서 Markdown 코드블록(````json`, ``` 등)과
	 * 불필요한 텍스트를 제거하고 순수 JSON 본문만 추출
	 */
	public static String extractPureJson(String text) {
		if (text == null || text.isBlank()) {
			return text;
		}

		String cleaned = text.trim();

		// 1. Markdown 코드블록 제거 (```json, ``` 등)
		if (cleaned.startsWith("```")) {
			cleaned = cleaned.replace("```json", "")
					.replace("```", "")
					.trim();
		}

		// 2. JSON 영역만 추출 (처음 '{' ~ 마지막 '}')
		int start = cleaned.indexOf("{");
		int end = cleaned.lastIndexOf("}");

		if (start >= 0 && end >= 0 && end > start) {
			cleaned = cleaned.substring(start, end + 1);
		}

		return cleaned.trim();
	}
}
