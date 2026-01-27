package com.aiinsightagent.core.parser;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightResponse;
import com.aiinsightagent.core.queue.GeminiResponse;
import com.google.genai.types.Candidate;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiResponseParserTest {
	@Test
	@DisplayName("Gemini 응답 JSON 파싱 성공 테스트")
	void toInsightResponse_success() {

		// given
		GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);

		String rawResponse = """
				```json
				{
				  "summary": "요약 내용",
				  "issueCategories": [
				    {
				      "category": "성능",
				      "description": "응답 시간이 느림",
				      "severity": "HIGH"
				    }
				  ],
				  "rootCauseInsights": ["네트워크 지연"],
				  "recommendedActions": ["캐시 적용"],
				  "priorityScore": 5
				}
				```
				""";

		when(contentResponse.text()).thenReturn(rawResponse);
		GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

		// when
		InsightResponse result = GeminiResponseParser.toInsightResponse(geminiResponse);

		// then
		assertNotNull(result);
		assertEquals(HttpStatus.OK.value(), result.getResultCode());
		assertEquals(HttpStatus.OK.getReasonPhrase(), result.getResultMsg());

		assertNotNull(result.getInsight());
		assertEquals("요약 내용", result.getInsight().getSummary());
		assertEquals(5, result.getInsight().getPriorityScore());
		assertEquals(1, result.getInsight().getIssueCategories().size());
		assertEquals("성능", result.getInsight().getIssueCategories().get(0).getCategory()
		);

		verify(contentResponse, times(1)).text();
	}

	@Nested
	@DisplayName("EMPTY_GEMINI_RESPONSE 에러 테스트")
	class EmptyResponseTest {

		@Test
		@DisplayName("응답 텍스트가 null인 경우 EMPTY_GEMINI_RESPONSE 예외 발생")
		void toInsightResponse_nullText_throwsEmptyGeminiResponseException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			when(contentResponse.text()).thenReturn(null);
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.EMPTY_GEMINI_RESPONSE.getCode(), exception.getError().getCode());
		}

		@Test
		@DisplayName("응답 텍스트가 빈 문자열인 경우 EMPTY_GEMINI_RESPONSE 예외 발생")
		void toInsightResponse_emptyText_throwsEmptyGeminiResponseException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			when(contentResponse.text()).thenReturn("");
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.EMPTY_GEMINI_RESPONSE.getCode(), exception.getError().getCode());
		}

		@Test
		@DisplayName("응답 텍스트가 공백만 있는 경우 EMPTY_GEMINI_RESPONSE 예외 발생")
		void toInsightResponse_blankText_throwsEmptyGeminiResponseException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			when(contentResponse.text()).thenReturn("   \n\t  ");
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.EMPTY_GEMINI_RESPONSE.getCode(), exception.getError().getCode());
		}

		@Test
		@DisplayName("JSON 블록이 없는 텍스트인 경우 FAIL_JSON_PARSING 예외 발생")
		void toInsightResponse_noJsonBlock_throwsJsonParsingException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			when(contentResponse.text()).thenReturn("This is just plain text without JSON");
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.FAIL_JSON_PARSING.getCode(), exception.getError().getCode());
		}
	}

	@Nested
	@DisplayName("RESPONSE_TRUNCATED 에러 테스트")
	class ResponseTruncatedTest {

		@Test
		@DisplayName("finishReason이 MAX_TOKENS인 경우 RESPONSE_TRUNCATED 예외 발생")
		void toInsightResponse_maxTokensFinishReason_throwsResponseTruncatedException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			Candidate candidate = mock(Candidate.class);
			FinishReason finishReason = mock(FinishReason.class);

			when(finishReason.toString()).thenReturn("MAX_TOKENS");
			when(candidate.finishReason()).thenReturn(Optional.of(finishReason));
			when(contentResponse.candidates()).thenReturn(Optional.of(List.of(candidate)));

			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.RESPONSE_TRUNCATED.getCode(), exception.getError().getCode());
		}

		@Test
		@DisplayName("finishReason이 LENGTH를 포함하는 경우 RESPONSE_TRUNCATED 예외 발생")
		void toInsightResponse_lengthFinishReason_throwsResponseTruncatedException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			Candidate candidate = mock(Candidate.class);
			FinishReason finishReason = mock(FinishReason.class);

			when(finishReason.toString()).thenReturn("LENGTH_EXCEEDED");
			when(candidate.finishReason()).thenReturn(Optional.of(finishReason));
			when(contentResponse.candidates()).thenReturn(Optional.of(List.of(candidate)));

			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.RESPONSE_TRUNCATED.getCode(), exception.getError().getCode());
		}

		@Test
		@DisplayName("JSON 구조가 불완전한 경우 (중괄호 불일치) RESPONSE_TRUNCATED 예외 발생")
		void toInsightResponse_incompleteBraces_throwsResponseTruncatedException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			String truncatedJson = """
					```json
					{
					  "summary": "요약 내용",
					  "issueCategories": [
					    {
					      "category": "성능"
					""";

			when(contentResponse.text()).thenReturn(truncatedJson);
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.RESPONSE_TRUNCATED.getCode(), exception.getError().getCode());
		}

		@Test
		@DisplayName("JSON 구조가 불완전한 경우 (대괄호 불일치) RESPONSE_TRUNCATED 예외 발생")
		void toInsightResponse_incompleteBrackets_throwsResponseTruncatedException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			String truncatedJson = """
					```json
					{
					  "summary": "요약 내용",
					  "issueCategories": [
					    {"category": "성능"},
					    {"category": "보안"
					""";

			when(contentResponse.text()).thenReturn(truncatedJson);
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.RESPONSE_TRUNCATED.getCode(), exception.getError().getCode());
		}
	}

	@Nested
	@DisplayName("FAIL_JSON_PARSING 에러 테스트")
	class JsonParsingFailureTest {

		@Test
		@DisplayName("유효하지 않은 JSON 형식인 경우 FAIL_JSON_PARSING 예외 발생")
		void toInsightResponse_invalidJson_throwsJsonParsingException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			String invalidJson = """
					```json
					{
					  "summary": "요약 내용",
					  invalid_key_without_quotes: "값"
					}
					```
					""";

			when(contentResponse.text()).thenReturn(invalidJson);
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.FAIL_JSON_PARSING.getCode(), exception.getError().getCode());
		}

		@Test
		@DisplayName("필수 필드가 누락된 JSON인 경우에도 파싱은 성공 (Jackson 기본 동작)")
		void toInsightResponse_missingFields_parsesSuccessfully() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			String minimalJson = """
					```json
					{
					  "summary": "요약만 있는 응답"
					}
					```
					""";

			when(contentResponse.text()).thenReturn(minimalJson);
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when
			InsightResponse result = GeminiResponseParser.toInsightResponse(geminiResponse);

			// then
			assertNotNull(result);
			assertEquals("요약만 있는 응답", result.getInsight().getSummary());
		}

		@Test
		@DisplayName("JSON 타입 불일치 시 FAIL_JSON_PARSING 예외 발생")
		void toInsightResponse_typeMismatch_throwsJsonParsingException() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			String typeMismatchJson = """
					```json
					{
					  "summary": "요약 내용",
					  "priorityScore": "숫자가_아닌_문자열"
					}
					```
					""";

			when(contentResponse.text()).thenReturn(typeMismatchJson);
			when(contentResponse.candidates()).thenReturn(Optional.empty());
			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when & then
			InsightException exception = assertThrows(InsightException.class,
					() -> GeminiResponseParser.toInsightResponse(geminiResponse));

			assertEquals(InsightError.FAIL_JSON_PARSING.getCode(), exception.getError().getCode());
		}
	}

	@Nested
	@DisplayName("정상 케이스 추가 테스트")
	class SuccessTest {

		@Test
		@DisplayName("finishReason이 STOP인 경우 정상 처리")
		void toInsightResponse_stopFinishReason_success() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			Candidate candidate = mock(Candidate.class);
			FinishReason finishReason = mock(FinishReason.class);

			when(finishReason.toString()).thenReturn("STOP");
			when(candidate.finishReason()).thenReturn(Optional.of(finishReason));
			when(contentResponse.candidates()).thenReturn(Optional.of(List.of(candidate)));

			String validJson = """
					```json
					{
					  "summary": "정상 응답",
					  "priorityScore": 3
					}
					```
					""";
			when(contentResponse.text()).thenReturn(validJson);

			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when
			InsightResponse result = GeminiResponseParser.toInsightResponse(geminiResponse);

			// then
			assertNotNull(result);
			assertEquals(HttpStatus.OK.value(), result.getResultCode());
			assertEquals("정상 응답", result.getInsight().getSummary());
		}

		@Test
		@DisplayName("candidates가 비어있는 경우에도 텍스트가 있으면 정상 처리")
		void toInsightResponse_emptyCandidates_success() {
			// given
			GenerateContentResponse contentResponse = mock(GenerateContentResponse.class);
			when(contentResponse.candidates()).thenReturn(Optional.empty());

			String validJson = """
					```json
					{
					  "summary": "candidates 없는 응답",
					  "priorityScore": 5
					}
					```
					""";
			when(contentResponse.text()).thenReturn(validJson);

			GeminiResponse geminiResponse = new GeminiResponse(contentResponse, "m01", "gemini-2.5-flash");

			// when
			InsightResponse result = GeminiResponseParser.toInsightResponse(geminiResponse);

			// then
			assertNotNull(result);
			assertEquals("candidates 없는 응답", result.getInsight().getSummary());
		}
	}
}