package com.aiinsightagent.core.prompt;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UserPrompt {
	private String dataKey;
	private Map<String, String> data;
}