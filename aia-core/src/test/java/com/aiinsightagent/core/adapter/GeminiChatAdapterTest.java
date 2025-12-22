package com.aiinsightagent.core.adapter;

import com.aiinsightagent.core.config.GeminiProperties;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class GeminiChatAdapterTest {
	private GeminiChatAdapter geminiChatAdapter;
	private GeminiProperties geminiProperties;
	private Models models;

	@BeforeEach
	void setUp() {
		geminiProperties = Mockito.mock(GeminiProperties.class);
		models = Mockito.mock(Models.class);

		Mockito.when(geminiProperties.getModel())
				.thenReturn("gemini-2.5-flash");

		geminiChatAdapter = new GeminiChatAdapter(geminiProperties, models);
	}

	@Test
	void getResponse_success() {

		// given
		String prompt = "test prompt";
		GenerateContentResponse mockResponse = Mockito.mock(GenerateContentResponse.class);

		Mockito.when(
				models.generateContent(
						anyString(),
						anyString(),
						isNull()
				)
		).thenReturn(mockResponse);

		// when
		GenerateContentResponse result =
				geminiChatAdapter.getResponse(prompt);

		// then
		assertNotNull(result);
		assertEquals(mockResponse, result);

		Mockito.verify(models).generateContent(
				"gemini-2.5-flash",
				prompt,
				null
		);
	}
}