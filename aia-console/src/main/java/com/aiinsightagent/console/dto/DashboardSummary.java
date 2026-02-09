package com.aiinsightagent.console.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummary {

	private long totalAnalysis;
	private long successCount;
	private long failedCount;
	private String appHealthStatus;
}
