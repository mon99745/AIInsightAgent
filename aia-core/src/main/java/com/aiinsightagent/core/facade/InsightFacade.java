package com.aiinsightagent.core.facade;

import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.aiinsightagent.core.parser.GeminiResponseParser;
import com.aiinsightagent.core.util.PromptComposer;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.model.prompt.SystemPrompt;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class InsightFacade {
	private final GeminiChatAdapter geminiChatAdapter;
	private final PromptComposer promptComposer;

	public InsightResponse answer(String purpose, String userPrompt) {
		String finalPrompt = promptComposer.getCombinedPrompt(
				purpose,
				SystemPrompt.SINGLE_ITEM,
				userPrompt);

		log.debug("Final Prompt: {}", finalPrompt);

		return GeminiResponseParser.toInsightResponse(geminiChatAdapter.getResponse(finalPrompt));
	}

	public InsightResponse analysis(InsightRequest request, String context) {
		List<UserPrompt> userPrompts = request.getUserPrompt();

		String combinedUserPrompt = userPrompts.stream()
				.map(promptComposer::getCombinedUserPrompt)
				.collect(Collectors.joining("\n\n"));

		log.debug("Combined User Prompt:\n str-length={}", combinedUserPrompt.length());

		String finalPrompt = promptComposer.getCombinedPrompt(
				request.getPurpose(),
				SystemPrompt.MULTI_ITEM,
				combinedUserPrompt
		);

		log.debug("Final Prompt:\n str-length={} \n finalPrompt={}", finalPrompt.length(), finalPrompt);

		return GeminiResponseParser.toInsightResponse(geminiChatAdapter.getResponse(finalPrompt));
	}
}