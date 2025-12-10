package com.aiinsightagent.core.prompt;

public class SystemPrompts {
	private SystemPrompts() {
	}

	public static final String SINGLE_ITEM = """
			    You are a data analysis assistant.
			    Analyze the text and provide concise insight.
			    Respond in plain text or JSON format.
			""";

	public static final String MULTI_ITEM = """
			    You are an AI analyst.
			    Analyze each item carefully and provide structured insights.
			    Respond in JSON format suitable for parsing into InsightResponse.
			""";
}
