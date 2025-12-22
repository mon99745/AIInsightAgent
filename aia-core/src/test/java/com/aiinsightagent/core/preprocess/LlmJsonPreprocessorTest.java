package com.aiinsightagent.core.preprocess;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LlmJsonPreprocessorTest {
	@Test
	@DisplayName("Markdown 코드블록이 포함된 LLM 응답에서 순수 JSON 추출 성공")
	void extractPureJson_success_withMarkdownBlock() {

		// given
		String raw = """
				```json
				{
				  "summary": "요약 내용",
				  "priorityScore": 3
				}
				```
				""";

		// when
		String result = LlmJsonPreprocessor.extractPureJson(raw);

		// then
		String expected = """
				{
				  "summary": "요약 내용",
				  "priorityScore": 3
				}
				""".trim();

		assertEquals(expected, result);
	}

	@Test
	@DisplayName("Markdown 없이 설명 문구가 포함된 응답에서 JSON 추출 성공")
	void extractPureJson_success_withTextAroundJson() {

		// given
		String raw = """
				아래는 요청하신 결과입니다.

				{
				  "summary": "분석 결과",
				  "priorityScore": 5
				}

				감사합니다.
				""";

		// when
		String result = LlmJsonPreprocessor.extractPureJson(raw);

		// then
		String expected = """
				{
				  "summary": "분석 결과",
				  "priorityScore": 5
				}
				""".trim();

		assertEquals(expected, result);
	}

	@Test
	@DisplayName("이미 순수 JSON인 경우 그대로 반환")
	void extractPureJson_success_pureJson() {

		// given
		String raw = """
				{
				  "summary": "순수 JSON",
				  "priorityScore": 1
				}
				""";

		// when
		String result = LlmJsonPreprocessor.extractPureJson(raw);

		// then
		assertEquals(raw.trim(), result);
	}
}