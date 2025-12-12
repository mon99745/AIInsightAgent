package com.aiinsightagent.core.prompt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinalPrompt {
	String purpose;
	String systemPrompt;
	String userPrompt;
}