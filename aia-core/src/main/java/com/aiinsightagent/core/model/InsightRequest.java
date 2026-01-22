package com.aiinsightagent.core.model;

import com.aiinsightagent.core.model.prompt.UserPrompt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InsightRequest {
	@NotBlank(message = "userId must not be blank")
	private String userId;

	@NotBlank(message = "purpose must not be blank")
	private String purpose;

	@NotEmpty(message = "userPrompt must not be empty")
	private List<UserPrompt> userPrompt;
}