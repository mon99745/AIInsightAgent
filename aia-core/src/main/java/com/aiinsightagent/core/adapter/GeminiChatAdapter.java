package com.aiinsightagent.core.adapter;

import com.aiinsightagent.core.config.GeminiProperties;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiChatAdapter {
	protected final GeminiProperties geminiProperties;
	private final Client geminiClient;

	/**
	 * Gemini Chat 응답 생성
	 * https://aistudio.google.com/app/
	 *
	 * @param prompt
	 * @return GenerateContentResponse
	 */
	public GenerateContentResponse getResponse(String prompt) {
		log.info("Model={}, Prompt={}", geminiProperties.getModel(), prompt);
		return geminiClient.models.generateContent(
				geminiProperties.getModel(),
				prompt,
				null
		);
	}
}