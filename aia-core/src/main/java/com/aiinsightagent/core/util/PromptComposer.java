package com.aiinsightagent.core.util;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.prompt.FinalPrompt;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromptComposer {
	private final ObjectMapper objectMapper;

	public String getCombinedPrompt(String purpose, String systemPrompt, String userPrompt) {
		if (userPrompt == null) {
			throw new InsightException(InsightError.EMPTY_USER_PROMPT);
		}
		String finalPrompt;
		FinalPrompt prompt = FinalPrompt.builder()
				.purpose(purpose)
				.systemPrompt(systemPrompt)
				.userPrompt(userPrompt)
				.build();

		try {
			finalPrompt = objectMapper.writeValueAsString(prompt);
		} catch (JsonProcessingException e) {
			throw new InsightException(InsightError.PROMPT_COMPOSITION_FAILURE);
		}

		return finalPrompt;
	}

	public String getCombinedUserPrompt(UserPrompt userPrompt) {
		if (userPrompt == null) {
			throw new InsightException(InsightError.EMPTY_USER_PROMPT);
		}

		StringBuilder sb = new StringBuilder();

		// 1. 데이터 키 (컨텍스트 식별자)
		if (userPrompt.getDataKey() != null && !userPrompt.getDataKey().isBlank()) {
			sb.append("[").append(userPrompt.getDataKey()).append("]")
					.append("\n");
		}

		// 2. 데이터 본문
		Map<String, String> data = userPrompt.getData();
		if (data != null && !data.isEmpty()) {
			for (Map.Entry<String, String> entry : data.entrySet()) {
				sb.append("- ")
						.append(entry.getKey())
						.append(": ")
						.append(entry.getValue())
						.append("\n");
			}
		}

		return sb.toString().trim();
	}
}
