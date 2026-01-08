package com.aiinsightagent.core.model.prompt;

public class SystemPrompt {
	private SystemPrompt() {
	}

	public static final String COMMON_COMMENT = """
            You are a data analysis assistant. 
            Your job is to analyze the provided text and strictly return the result in the specified JSON format below.

            [OUTPUT FORMAT]
            {
              "summary": "텍스트 요약",
              "issueCategories": [
                {
                  "category": "카테고리명",
                  "description": "문제 설명",
                  "severity": "HIGH | MEDIUM | LOW"
                }
              ],
              "rootCauseInsights": ["문제 원인 분석"],
              "recommendedActions": ["개선 제안"],
              "priorityScore": 1
            }

            [RULES]
            1. 반드시 위 JSON 구조만 반환한다.
            2. 모든 필드를 반드시 채운다.
            3. severity는 HIGH, MEDIUM, LOW 중 하나로 채운다.
            4. priorityScore는 1~100 사이 정수로 선정한다.
            5. JSON 외의 텍스트, 설명, 인사말은 절대 출력하지 않는다.
            6. null, empty 값은 절대 허용하지 않는다.
            7. [CONTEXT] 섹션의 데이터를 사전 지식으로 활용하여 분석한다.
            8. 입력 데이터에 null 이나 빈 값이 있어도 무시하고 분석하며, 결과에 이를 언급하지 않는다.
            9. 모든 응답은 한국어로 작성한다.
            """;

	public static final String  SINGLE_ITEM = COMMON_COMMENT + """
			    Analyze the text and provide concise insight.
			    Respond in plain text or JSON format.
			""";

	public static final String MULTI_ITEM = COMMON_COMMENT + """
			    Analyze each item carefully and provide structured insights.
			    Respond in JSON format suitable for parsing into InsightResponse .
			""";
}