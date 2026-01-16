package com.aiinsightagent.core.util;

import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
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
	@DisplayName("getCombinedUserPrompts - 복수 항목 순번 기반 결합 성공")
	void getCombinedUserPrompts_success() {

		// given
		Map<String, String> data1 = new LinkedHashMap<>();
		data1.put("cpu_usage", "85%");
		data1.put("memory_usage", "70%");

		Map<String, String> data2 = new LinkedHashMap<>();
		data2.put("cpu_usage", "60%");
		data2.put("memory_usage", "50%");

		UserPrompt userPrompt1 = UserPrompt.builder()
				.dataKey("SERVER_1")
				.data(data1)
				.build();

		UserPrompt userPrompt2 = UserPrompt.builder()
				.dataKey("SERVER_2")
				.data(data2)
				.build();

		// when
		String result = promptComposer.getCombinedUserPrompts(List.of(userPrompt1, userPrompt2));

		// then
		String expected =
				"#1\n" +
				"cpu_usage=85%\n" +
				"memory_usage=70%\n\n" +
				"#2\n" +
				"cpu_usage=60%\n" +
				"memory_usage=50%";

		assertEquals(expected, result);
	}

	@Test
	@DisplayName("getCombinedUserPrompts - 단일 항목 성공")
	void getCombinedUserPrompts_singleItem() {

		// given
		Map<String, String> data = new LinkedHashMap<>();
		data.put("value", "100");

		UserPrompt userPrompt = UserPrompt.builder()
				.dataKey("ITEM")
				.data(data)
				.build();

		// when
		String result = promptComposer.getCombinedUserPrompts(List.of(userPrompt));

		// then
		assertEquals("#1\nvalue=100", result);
	}
}