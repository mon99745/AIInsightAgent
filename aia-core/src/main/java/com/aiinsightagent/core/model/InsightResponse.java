package com.aiinsightagent.core.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InsightResponse {
	private int resultCode;
	private String resultMsg;
	private InsightDetail insight;
}