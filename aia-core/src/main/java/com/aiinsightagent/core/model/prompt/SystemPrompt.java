package com.aiinsightagent.core.model.prompt;

public class SystemPrompt {
	private SystemPrompt() {
	}

	public static final String COMMON_COMMENT = """
			JSON만 반환.{"summary":"","issueCategories":[{"category":"","description":"","severity":"HIGH|MEDIUM|LOW"}],"rootCauseInsights":[],"recommendedActions":[],"priorityScore":1-100}
			한국어,모든필드필수,CONTEXT=사전지식,null무시""";

	public static final String SINGLE_ITEM = COMMON_COMMENT + ",단일항목,간결히";

	public static final String MULTI_ITEM = COMMON_COMMENT + ",복수항목,각항목별구조화";
}