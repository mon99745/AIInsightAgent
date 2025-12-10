package com.aiinsightagent.core.service;

import com.aiinsightagent.core.message.InsightRequest;
import com.aiinsightagent.core.message.InsightResponse;
import com.aiinsightagent.core.prompt.SystemPrompts;
import com.aiinsightagent.core.util.GeminiResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InsightService {
	private final GeminiChatService chatService;

	public InsightResponse getSingleAnswer(String purpose, String userPrompt) {
		log.info("[getSingleAnswer] Generating insight for prompt: {}", userPrompt);
		String combinedPrompt = SystemPrompts.SINGLE_ITEM +
				"\nOperation: " + purpose +
				"\nUser input:\n" + userPrompt;

		return GeminiResponseParser.toInsightResponse(chatService.getResponse(combinedPrompt));
	}

	public InsightResponse getMultipleAnswer(InsightRequest request) {
		log.info("[getMultipleAnswer] Generating insight for prompt: {}", request.getUserPrompt());

		// userPrompt 리스트를 문자열로 변환
		StringBuilder userInputBuilder = new StringBuilder();


		String combinedPrompt = SystemPrompts.MULTI_ITEM +
				"\nPurpose: " + request.getPurpose() +
				"\nUser input:\n" + userInputBuilder.toString();

		return GeminiResponseParser.toInsightResponse(chatService.getResponse(combinedPrompt));
	}
}