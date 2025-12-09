package com.aiinsightagent.core.service;

import com.aiinsightagent.core.config.GeminiProperties;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiChatService {
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
		return geminiClient.models.generateContent(
				geminiProperties.getModel(),
				prompt,
				null
		);
	}
}