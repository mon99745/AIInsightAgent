package com.aiinsightagent.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightDetail {
	private String summary;
	private List<IssueCategory> issueCategories;
	private List<String> rootCauseInsights;
	private List<String> recommendedActions;
	private int priorityScore;
}
