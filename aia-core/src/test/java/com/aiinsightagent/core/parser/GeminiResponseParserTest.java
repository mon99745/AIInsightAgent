package com.aiinsightagent.core.parser;

import com.aiinsightagent.core.model.InsightResponse;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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
		GenerateContentResponse response = mock(GenerateContentResponse.class);

		String rawResponse = """
				```json
				{
				  "summary": "요약 내용",
				  "issueCategories": [
				    {
				      "category": "성능",
				      "description": "응답 시간이 느림"
				    }
				  ],
				  "rootCauseInsights": ["네트워크 지연"],
				  "recommendedActions": ["캐시 적용"],
				  "priorityScore": 5
				}
				```
				""";

		when(response.text()).thenReturn(rawResponse);

		// when
		InsightResponse result = GeminiResponseParser.toInsightResponse(response);

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

		verify(response, times(1)).text();
	}
}