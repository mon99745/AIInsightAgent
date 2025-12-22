package com.aiinsightagent.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCategory {
	private String category;
	private String description;
	private String severity;   // HIGH / MEDIUM / LOW
}