package com.aiinsightagent.core.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InsightHistoryResponse {
	private int resultCode;
	private String resultMsg;
	private List<InsightRecord> insightRecords;
}