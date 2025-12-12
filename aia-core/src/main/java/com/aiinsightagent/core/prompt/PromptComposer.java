package com.aiinsightagent.core.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromptComposer {
	private final ObjectMapper objectMapper;

	public String getCombinedPrompt(String purpose, String systemPrompt, String userPrompt) {
		String finalPrompt;
		FinalPrompt prompt = FinalPrompt.builder()
				.purpose(purpose)
				.systemPrompt(systemPrompt)
				.userPrompt(userPrompt)
				.build();

		try {
			finalPrompt = objectMapper.writeValueAsString(prompt);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to convert Prompt to JSON string", e);
		}

		return finalPrompt;
	}
}
