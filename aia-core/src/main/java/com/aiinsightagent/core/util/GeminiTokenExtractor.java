package com.aiinsightagent.core.util;

import com.aiinsightagent.core.model.TokenUsage;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;

import java.util.Optional;

public final class GeminiTokenExtractor {

	private GeminiTokenExtractor() {}

	/**
	 * GenerateContentResponse 로부터 토큰 사용량 추출
	 *
	 * @param response Gemini API 응답
	 * @return TokenUsage (토큰 정보가 없으면 모두 0)
	 */
	public static TokenUsage extract(GenerateContentResponse response) {

		if (response == null) {
			return new TokenUsage(0, 0, 0);
		}

		Optional<GenerateContentResponseUsageMetadata> usageOpt =
				response.usageMetadata();

		if (usageOpt.isEmpty()) {
			return new TokenUsage(0, 0, 0);
		}

		GenerateContentResponseUsageMetadata usage = usageOpt.get();

		int promptTokens =
				usage.promptTokenCount().orElse(0);

		int completionTokens =
				usage.candidatesTokenCount().orElse(0);

		int totalTokens =
				usage.totalTokenCount().orElse(
						promptTokens + completionTokens
				);

		return new TokenUsage(
				promptTokens,
				completionTokens,
				totalTokens
		);
	}
}
