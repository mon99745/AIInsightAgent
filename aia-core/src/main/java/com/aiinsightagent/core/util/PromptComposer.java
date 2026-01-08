package com.aiinsightagent.core.util;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.prompt.FinalPrompt;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptComposer {
	private final ObjectMapper objectMapper;

	public String getCombinedPrompt(String purpose, String systemPrompt, String context, String userPrompt) {
		if (userPrompt == null) {
			throw new InsightException(InsightError.EMPTY_USER_PROMPT);
		}

		String enhancedSystemPrompt = buildSystemPrompt(systemPrompt, context);

		FinalPrompt prompt = FinalPrompt.builder()
				.purpose(purpose)
				.systemPrompt(enhancedSystemPrompt)
				.userPrompt(userPrompt)
				.build();

		try {
			return objectMapper.writeValueAsString(prompt);
		} catch (JsonProcessingException e) {
			throw new InsightException(InsightError.PROMPT_COMPOSITION_FAILURE);
		}
	}

	private String buildSystemPrompt(String systemPrompt, String context) {
		if (context == null || context.isBlank()) {
			return systemPrompt;
		}

		return systemPrompt + "\n\n[CONTEXT]\n" + context;
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