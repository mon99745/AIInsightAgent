package com.aiinsightagent.core.facade;

import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.aiinsightagent.core.parser.GeminiResponseParser;
import com.aiinsightagent.core.prompt.PromptComposer;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.prompt.SystemPrompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InsightFacade {
	private final GeminiChatAdapter chatService;
	private final PromptComposer promptService;

	public InsightResponse answer(String purpose, String userPrompt) {
		String finalPrompt = promptService.getCombinedPrompt(purpose, SystemPrompt.SINGLE_ITEM, userPrompt);

		return GeminiResponseParser.toInsightResponse(chatService.getResponse(finalPrompt));
	}

	public InsightResponse analysis(InsightRequest request) {
		StringBuilder userInputBuilder = new StringBuilder();

		String combinedPrompt = SystemPrompt.MULTI_ITEM +
				"\nPurpose: " + request.getPurpose() +
				"\nUser input:\n" + userInputBuilder.toString();

		return GeminiResponseParser.toInsightResponse(chatService.getResponse(combinedPrompt));
	}
}