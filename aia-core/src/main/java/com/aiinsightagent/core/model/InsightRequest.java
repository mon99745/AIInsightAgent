package com.aiinsightagent.core.model;

import com.aiinsightagent.core.model.prompt.UserPrompt;
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