package com.aiinsightagent.core.queue;

import com.google.genai.types.GenerateContentResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Gemini API 응답과 처리한 워커의 모델 정보를 포함하는 래퍼 클래스
 */
@Getter
@RequiredArgsConstructor
public class GeminiResponse {
    private final GenerateContentResponse response;
    private final String modelId;
    private final String modelName;
}
