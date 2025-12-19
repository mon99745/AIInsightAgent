package com.aiinsightagent.core.facade;

import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.aiinsightagent.core.parser.GeminiResponseParser;
import com.aiinsightagent.core.prompt.PromptComposer;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.prompt.SystemPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InsightFacade {
	private final GeminiChatAdapter geminiChatAdapter;
	private final PromptComposer promptService;

	public InsightResponse answer(String purpose, String userPrompt) {
		String finalPrompt = promptService.getCombinedPrompt(
				purpose,
				SystemPrompt.SINGLE_ITEM,
				userPrompt);

		return GeminiResponseParser.toInsightResponse(geminiChatAdapter.getResponse(finalPrompt));
	}

	public InsightResponse analysis(InsightRequest request) {
		String finalPrompt = promptService.getCombinedPrompt(
				request.getPurpose(),
				SystemPrompt.MULTI_ITEM,
				request.getUserPrompt().get(0).getData().toString()
		);

		return GeminiResponseParser.toInsightResponse(geminiChatAdapter.getResponse(finalPrompt));
	}
}