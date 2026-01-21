package com.aiinsightagent.core.model.prompt;

public class SystemPrompt {
    private SystemPrompt() {
    }

    public static final String COMMON_COMMENT = """
            JSON만 반환.
            아래 스키마를 반드시 준수할 것.
            
            {
              "summary": "string",
              "issueCategories": [
                {
                  "category": "string",
                  "description": "string",
                  "severity":"HIGH|MEDIUM|LOW"
                }
              ],
              "rootCauseInsights": ["string"],
              "recommendedActions": ["string"],
              "priorityScore": 1-100
            }
            
            규칙:
            - 한국어
            - 모든 필드 필수
            - 배열은 최소 1개 이상
            - JSON 외 텍스트 출력 금지
            """;


    public static final String SINGLE_ITEM = COMMON_COMMENT + ",단일항목,간결히";

    public static final String MULTI_ITEM = COMMON_COMMENT + ",복수항목,각항목별구조화";
}