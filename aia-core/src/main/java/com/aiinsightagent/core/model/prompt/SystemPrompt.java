package com.aiinsightagent.core.model.prompt;

public class SystemPrompt {
	private SystemPrompt() {
	}

	public static final String COMMON_COMMENT = """
			JSON만 반환. 설명/인사말 금지.

			{"summary":"요약","issueCategories":[{"category":"카테고리","description":"설명","severity":"HIGH|MEDIUM|LOW"}],"rootCauseInsights":["원인"],"recommendedActions":["제안"],"priorityScore":1}

			규칙: 한국어, 모든필드필수, priorityScore 1-100, CONTEXT는 사전지식으로 활용, null/빈값 무시하고 분석
			""";

	public static final String SINGLE_ITEM = COMMON_COMMENT + """
			단일 항목 분석. 간결하게.
			""";

	public static final String MULTI_ITEM = COMMON_COMMENT + """
			복수 항목 분석. 각 항목별 구조화.
			""";
}