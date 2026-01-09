package com.aiinsightagent.core.util;

import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromptComposerTest {
	private PromptComposer promptComposer;

	@BeforeEach
	void setUp() {
		ObjectMapper objectMapper = new ObjectMapper();
		promptComposer = new PromptComposer(objectMapper);
	}

	@Test
	@DisplayName("getCombinedPrompt - 목적/시스템프롬프트/유저프롬프트 JSON 결합 성공")
	void getCombinedPrompt_success() {

		// given
		String purpose = "INSIGHT_ANALYSIS";
		String systemPrompt = "You are an expert analyst.";
		String userPrompt = "Analyze the following data.";

		// when
		String result = promptComposer.getCombinedPrompt(
				purpose,
				systemPrompt,
				null,
				userPrompt
		);

		// then
		assertNotNull(result);
		assertTrue(result.contains("\"purpose\":\"INSIGHT_ANALYSIS\""));
		assertTrue(result.contains("\"systemPrompt\":\"You are an expert analyst.\""));
		assertTrue(result.contains("\"userPrompt\":\"Analyze the following data.\""));
	}

	@Test
	@DisplayName("getCombinedUserPrompt - dataKey + data Map 결합 성공")
	void getCombinedUserPrompt_success() {

		// given
		Map<String, String> data = new LinkedHashMap<>();
		data.put("cpu_usage", "85%");
		data.put("memory_usage", "70%");

		UserPrompt userPrompt = UserPrompt.builder()
				.dataKey("SERVER_STATUS")
				.data(data)
				.build();

		// when
		String result = promptComposer.getCombinedUserPrompt(userPrompt);

		// then
		String expected =
				"[SERVER_STATUS]\n" +
						"- cpu_usage: 85%\n" +
						"- memory_usage: 70%";

		assertEquals(expected, result);
	}

	@Test
	@DisplayName("getCombinedUserPrompt - dataKey만 존재하는 경우 성공")
	void getCombinedUserPrompt_onlyDataKey() {

		// given
		UserPrompt userPrompt = UserPrompt.builder()
				.dataKey("ONLY_KEY")
				.build();

		// when
		String result = promptComposer.getCombinedUserPrompt(userPrompt);

		// then
		assertEquals("[ONLY_KEY]", result);
	}
}