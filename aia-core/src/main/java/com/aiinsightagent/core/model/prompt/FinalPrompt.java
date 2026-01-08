package com.aiinsightagent.core.model.prompt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinalPrompt {
	private String purpose;
	private String systemPrompt;
	private String userPrompt;
}