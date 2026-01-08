package com.aiinsightagent.core.model;

import com.aiinsightagent.core.model.prompt.UserPrompt;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InsightRecord {
	private Long inputId;
	private LocalDateTime regDate;
	private UserPrompt userPrompt;
}