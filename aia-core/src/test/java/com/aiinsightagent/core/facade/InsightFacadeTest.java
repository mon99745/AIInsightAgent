package com.aiinsightagent.core.facade;

import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.model.prompt.SystemPrompt;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.aiinsightagent.core.parser.GeminiResponseParser;
import com.aiinsightagent.core.util.PromptComposer;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightFacadeTest {
	@Mock
	private GeminiChatAdapter geminiChatAdapter;

	@Mock
	private PromptComposer promptComposer;

	private InsightFacade insightFacade;

	@BeforeEach
	void setUp() {
		insightFacade = new InsightFacade(geminiChatAdapter, promptComposer);
	}

	@Test
	@DisplayName("answer() 성공 테스트")
	void answer_success() {

		// given
		String purpose = "요약";
		String userPrompt = "이 문장을 요약해줘";
		String finalPrompt = "FINAL_PROMPT";

		GenerateContentResponse geminiResponse = mock(GenerateContentResponse.class);

		InsightResponse expectedResponse =
				mock(InsightResponse.class);

		when(
				promptComposer.getCombinedPrompt(
						purpose,
						SystemPrompt.SINGLE_ITEM,
						null,
						userPrompt
				)
		).thenReturn(finalPrompt);

		when(geminiChatAdapter.getResponse(finalPrompt))
				.thenReturn(geminiResponse);

		try (MockedStatic<GeminiResponseParser> mocked =
					 Mockito.mockStatic(GeminiResponseParser.class)) {

			mocked.when(() ->
					GeminiResponseParser.toInsightResponse(geminiResponse)
			).thenReturn(expectedResponse);

			// when
			InsightResponse result = insightFacade.answer(purpose, userPrompt);

			// then
			assertNotNull(result);
			assertEquals(expectedResponse, result);

			verify(promptComposer, times(1))
					.getCombinedPrompt(
							purpose,
							SystemPrompt.SINGLE_ITEM,
							null,
							userPrompt
					);

			verify(geminiChatAdapter, times(1))
					.getResponse(finalPrompt);

			mocked.verify(
					() -> GeminiResponseParser.toInsightResponse(geminiResponse),
					times(1)
			);
		}
	}

	@Test
	@DisplayName("analysis() 성공 테스트")
	void analysis_success() {

		// given
		String purpose = "분석";

		UserPrompt userPrompt1 = mock(UserPrompt.class);
		UserPrompt userPrompt2 = mock(UserPrompt.class);

		List<UserPrompt> userPrompts =
				List.of(userPrompt1, userPrompt2);

		InsightRequest request = mock(InsightRequest.class);

		when(request.getPurpose()).thenReturn(purpose);
		when(request.getUserPrompt()).thenReturn(userPrompts);

		when(promptComposer.getCombinedUserPrompt(userPrompt1))
				.thenReturn("PROMPT_1");
		when(promptComposer.getCombinedUserPrompt(userPrompt2))
				.thenReturn("PROMPT_2");

		String combinedUserPrompt = "PROMPT_1\n\nPROMPT_2";
		String finalPrompt = "FINAL_ANALYSIS_PROMPT";

		when(
				promptComposer.getCombinedPrompt(
						purpose,
						SystemPrompt.MULTI_ITEM,
						null,
						combinedUserPrompt
				)
		).thenReturn(finalPrompt);

		GenerateContentResponse geminiResponse = mock(GenerateContentResponse.class);

		InsightResponse expectedResponse = mock(InsightResponse.class);

		when(geminiChatAdapter.getResponse(finalPrompt))
				.thenReturn(geminiResponse);

		try (MockedStatic<GeminiResponseParser> mocked =
					 Mockito.mockStatic(GeminiResponseParser.class)) {

			mocked.when(() ->
					GeminiResponseParser.toInsightResponse(geminiResponse)
			).thenReturn(expectedResponse);

			// when
			InsightResponse result = insightFacade.analysis(request, null);

			// then
			assertNotNull(result);
			assertEquals(expectedResponse, result);

			verify(promptComposer, times(1))
					.getCombinedPrompt(
							purpose,
							SystemPrompt.MULTI_ITEM,
							null,
							combinedUserPrompt
					);

			verify(geminiChatAdapter, times(1))
					.getResponse(finalPrompt);

			mocked.verify(
					() -> GeminiResponseParser.toInsightResponse(geminiResponse),
					times(1)
			);
		}
	}
}