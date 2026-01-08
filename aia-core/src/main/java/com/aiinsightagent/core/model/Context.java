package com.aiinsightagent.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class Context {
	private String userId;
	private String category;
	private Map<String, String> data;
}