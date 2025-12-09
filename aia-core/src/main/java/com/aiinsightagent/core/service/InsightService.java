package com.aiinsightagent.core.service;

import com.aiinsightagent.common.message.InsightResponse;
import com.aiinsightagent.core.util.GeminiResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InsightService {
	private final GeminiChatService chatService;

	public InsightResponse getAnswer(String prompt) {
		log.info("[getAnswer] Generating insight for prompt: {}", prompt);

		return GeminiResponseParser.toInsightResponse(chatService.getResponse(prompt));
	}
}