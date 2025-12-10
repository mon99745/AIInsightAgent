package com.aiinsightagent.core.message;

import com.aiinsightagent.core.prompt.UserPrompt;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InsightRequest {
	private String userId;
	private String purpose;
	private List<UserPrompt> userPrompt;
}