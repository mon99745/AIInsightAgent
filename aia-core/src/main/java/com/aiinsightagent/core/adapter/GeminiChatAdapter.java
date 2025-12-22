package com.aiinsightagent.core.adapter;

import com.aiinsightagent.core.config.GeminiProperties;
import com.aiinsightagent.core.model.TokenUsage;
import com.aiinsightagent.core.util.GeminiTokenExtractor;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiChatAdapter {
	protected final GeminiProperties geminiProperties;
	private final Models models;

	/**
	 * Gemini Chat 응답 생성
	 * https://aistudio.google.com/app/
	 *
	 * @param prompt
	 * @return response
	 */
	public GenerateContentResponse getResponse(String prompt) {
		GenerateContentResponse response = models.generateContent(
				geminiProperties.getModel(),
				prompt,
				null
		);

		TokenUsage tokenUsage = GeminiTokenExtractor.extract(response);

		log.debug(
				"[Gemini Token Usage] prompt={}, completion={}, total={}",
				tokenUsage.getPromptTokens(),
				tokenUsage.getCompletionTokens(),
				tokenUsage.getTotalTokens()
		);

		return response;
	}
}