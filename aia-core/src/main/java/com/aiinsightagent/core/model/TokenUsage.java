package com.aiinsightagent.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenUsage {
	private final int promptTokens;
	private final int completionTokens;
	private final int totalTokens;
}