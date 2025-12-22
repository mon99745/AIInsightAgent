package com.aiinsightagent.core.util;

import com.aiinsightagent.core.model.TokenUsage;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiTokenExtractorTest {
	@Test
	@DisplayName("Gemini 응답에서 토큰 사용량 추출 성공")
	void extract_success() {

		// given
		GenerateContentResponse response = mock(GenerateContentResponse.class);

		GenerateContentResponseUsageMetadata usageMetadata = mock(GenerateContentResponseUsageMetadata.class);

		when(response.usageMetadata())
				.thenReturn(Optional.of(usageMetadata));

		when(usageMetadata.promptTokenCount())
				.thenReturn(Optional.of(10));

		when(usageMetadata.candidatesTokenCount())
				.thenReturn(Optional.of(20));

		when(usageMetadata.totalTokenCount())
				.thenReturn(Optional.of(30));

		// when
		TokenUsage result =
				GeminiTokenExtractor.extract(response);

		// then
		assertNotNull(result);
		assertEquals(10, result.getPromptTokens());
		assertEquals(20, result.getCompletionTokens());
		assertEquals(30, result.getTotalTokens());

		verify(response, times(1)).usageMetadata();
		verify(usageMetadata, times(1)).promptTokenCount();
		verify(usageMetadata, times(1)).candidatesTokenCount();
		verify(usageMetadata, times(1)).totalTokenCount();
	}

	@Test
	@DisplayName("totalTokenCount가 없을 경우 prompt + completion 합산")
	void extract_success_withoutTotalTokenCount() {

		// given
		GenerateContentResponse response = mock(GenerateContentResponse.class);

		GenerateContentResponseUsageMetadata usageMetadata = mock(GenerateContentResponseUsageMetadata.class);

		when(response.usageMetadata())
				.thenReturn(Optional.of(usageMetadata));

		when(usageMetadata.promptTokenCount())
				.thenReturn(Optional.of(7));

		when(usageMetadata.candidatesTokenCount())
				.thenReturn(Optional.of(13));

		when(usageMetadata.totalTokenCount())
				.thenReturn(Optional.empty());

		// when
		TokenUsage result = GeminiTokenExtractor.extract(response);

		// then
		assertEquals(7, result.getPromptTokens());
		assertEquals(13, result.getCompletionTokens());
		assertEquals(20, result.getTotalTokens());
	}
}