package com.aiinsightagent.common.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InsightResponse {
	private int resultCode;
	private String resultMsg;
	private Object insight;
}