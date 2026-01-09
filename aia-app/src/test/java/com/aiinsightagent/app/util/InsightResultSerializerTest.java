package com.aiinsightagent.app.util;

import com.aiinsightagent.app.exception.InsightAppError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InsightResultSerializer 테스트")
class InsightResultSerializerTest {

	@Mock
	private ObjectMapper objectMapper;

	private InsightResultSerializer serializer;

	@Mock
	private InsightDetail insightDetail;

	@BeforeEach
	void setUp() {
		serializer = new InsightResultSerializer(objectMapper);
	}

	@Test
	@DisplayName("serialize - InsightDetail 직렬화 성공")
	void serialize_Success() throws JsonProcessingException {
		// given
		String expectedJson = "{\"runningStyle\":\"endurance\",\"averagePace\":\"6:00\"}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(expectedJson);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(expectedJson);

		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 복잡한 InsightDetail 직렬화 성공")
	void serialize_ComplexInsightDetail_Success() throws JsonProcessingException {
		// given
		String complexJson = "{\"runningStyle\":\"speed\"," +
				"\"averagePace\":\"4:30\"," +
				"\"statistics\":{\"avgHeartRate\":180,\"maxHeartRate\":195}," +
				"\"recommendations\":[\"페이스 조절\",\"회복 시간 증가\"]}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(complexJson);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(complexJson);
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 빈 InsightDetail 직렬화")
	void serialize_EmptyInsightDetail_Success() throws JsonProcessingException {
		// given
		String emptyJson = "{}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(emptyJson);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(emptyJson);
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - null InsightDetail 직렬화")
	void serialize_NullInsightDetail_Success() throws JsonProcessingException {
		// given
		InsightDetail nullDetail = null;
		String nullJson = "null";

		given(objectMapper.writeValueAsString(nullDetail))
				.willReturn(nullJson);

		// when
		String result = serializer.serialize(nullDetail);

		// then
		assertThat(result).isEqualTo(nullJson);
		verify(objectMapper, times(1)).writeValueAsString(nullDetail);
	}

	@Test
	@DisplayName("serialize - JsonProcessingException 발생 시 InsightException 발생")
	void serialize_JsonProcessingException_ThrowsInsightException() throws JsonProcessingException {
		// given
		given(objectMapper.writeValueAsString(insightDetail))
				.willThrow(new JsonProcessingException("JSON processing failed") {});

		// when & then
		assertThatThrownBy(() -> serializer.serialize(insightDetail))
				.isInstanceOf(InsightException.class)
				.extracting(e -> ((InsightException) e).getError())
				.isEqualTo(InsightAppError.FAIL_SERIALIZE_INSIGHT_DETAIL);

		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - RuntimeException 발생 시 InsightException 발생")
	void serialize_RuntimeException_ThrowsInsightException() throws JsonProcessingException {
		// given
		given(objectMapper.writeValueAsString(insightDetail))
				.willThrow(new RuntimeException("Unexpected error"));

		// when & then
		assertThatThrownBy(() -> serializer.serialize(insightDetail))
				.isInstanceOf(InsightException.class)
				.extracting(e -> ((InsightException) e).getError())
				.isEqualTo(InsightAppError.FAIL_SERIALIZE_INSIGHT_DETAIL);

		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - IllegalArgumentException 발생 시 InsightException 발생")
	void serialize_IllegalArgumentException_ThrowsInsightException() throws JsonProcessingException {
		// given
		given(objectMapper.writeValueAsString(insightDetail))
				.willThrow(new IllegalArgumentException("Invalid argument"));

		// when & then
		assertThatThrownBy(() -> serializer.serialize(insightDetail))
				.isInstanceOf(InsightException.class)
				.extracting(e -> ((InsightException) e).getError())
				.isEqualTo(InsightAppError.FAIL_SERIALIZE_INSIGHT_DETAIL);

		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 여러 InsightDetail 순차 직렬화")
	void serialize_MultipleInsightDetails_Success() throws JsonProcessingException {
		// given
		InsightDetail detail1 = mock(InsightDetail.class);
		InsightDetail detail2 = mock(InsightDetail.class);
		InsightDetail detail3 = mock(InsightDetail.class);

		String json1 = "{\"type\":\"detail1\"}";
		String json2 = "{\"type\":\"detail2\"}";
		String json3 = "{\"type\":\"detail3\"}";

		given(objectMapper.writeValueAsString(detail1)).willReturn(json1);
		given(objectMapper.writeValueAsString(detail2)).willReturn(json2);
		given(objectMapper.writeValueAsString(detail3)).willReturn(json3);

		// when
		String result1 = serializer.serialize(detail1);
		String result2 = serializer.serialize(detail2);
		String result3 = serializer.serialize(detail3);

		// then
		assertThat(result1).isEqualTo(json1);
		assertThat(result2).isEqualTo(json2);
		assertThat(result3).isEqualTo(json3);

		verify(objectMapper, times(1)).writeValueAsString(detail1);
		verify(objectMapper, times(1)).writeValueAsString(detail2);
		verify(objectMapper, times(1)).writeValueAsString(detail3);
	}

	@Test
	@DisplayName("serialize - 큰 사이즈의 JSON 직렬화")
	void serialize_LargeJson_Success() throws JsonProcessingException {
		// given
		StringBuilder largeJson = new StringBuilder("{");
		for (int i = 0; i < 1000; i++) {
			largeJson.append("\"field").append(i).append("\":\"value").append(i).append("\"");
			if (i < 999) largeJson.append(",");
		}
		largeJson.append("}");

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(largeJson.toString());

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(largeJson.length());
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 특수문자가 포함된 JSON 직렬화")
	void serialize_SpecialCharacters_Success() throws JsonProcessingException {
		// given
		String jsonWithSpecialChars = "{\"message\":\"테스트\\n줄바꿈\\t탭\",\"quote\":\"\\\"quoted\\\"\"}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(jsonWithSpecialChars);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(jsonWithSpecialChars);
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 유니코드가 포함된 JSON 직렬화")
	void serialize_Unicode_Success() throws JsonProcessingException {
		// given
		String jsonWithUnicode = "{\"message\":\"한글 메시지\",\"emoji\":\"🏃‍♂️💪\"}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(jsonWithUnicode);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(jsonWithUnicode);
		assertThat(result).contains("한글 메시지");
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 중첩된 구조의 JSON 직렬화")
	void serialize_NestedJson_Success() throws JsonProcessingException {
		// given
		String nestedJson = "{\"runningStyle\":\"endurance\"," +
				"\"statistics\":{" +
				"\"pace\":{\"average\":\"6:00\",\"best\":\"5:30\"}," +
				"\"heartRate\":{\"average\":180,\"max\":195}" +
				"}," +
				"\"history\":[" +
				"{\"date\":\"2024-01-01\",\"distance\":10}," +
				"{\"date\":\"2024-01-02\",\"distance\":15}" +
				"]}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(nestedJson);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(nestedJson);
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 동일한 InsightDetail 반복 직렬화")
	void serialize_SameInsightDetailMultipleTimes_Success() throws JsonProcessingException {
		// given
		String expectedJson = "{\"data\":\"test\"}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(expectedJson);

		// when
		String result1 = serializer.serialize(insightDetail);
		String result2 = serializer.serialize(insightDetail);
		String result3 = serializer.serialize(insightDetail);

		// then
		assertThat(result1).isEqualTo(expectedJson);
		assertThat(result2).isEqualTo(expectedJson);
		assertThat(result3).isEqualTo(expectedJson);

		verify(objectMapper, times(3)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - ObjectMapper가 빈 문자열 반환")
	void serialize_EmptyString_Success() throws JsonProcessingException {
		// given
		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn("");

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEmpty();
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 배열 형태의 JSON 직렬화")
	void serialize_JsonArray_Success() throws JsonProcessingException {
		// given
		String arrayJson = "[{\"id\":1,\"value\":\"first\"},{\"id\":2,\"value\":\"second\"}]";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(arrayJson);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(arrayJson);
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - 숫자 타입이 포함된 JSON 직렬화")
	void serialize_NumericValues_Success() throws JsonProcessingException {
		// given
		String jsonWithNumbers = "{\"distance\":10.5,\"duration\":3600,\"heartRate\":180.5}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(jsonWithNumbers);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(jsonWithNumbers);
		assertThat(result).contains("10.5");
		assertThat(result).contains("3600");
		assertThat(result).contains("180.5");
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - boolean 값이 포함된 JSON 직렬화")
	void serialize_BooleanValues_Success() throws JsonProcessingException {
		// given
		String jsonWithBooleans = "{\"isActive\":true,\"completed\":false}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(jsonWithBooleans);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(jsonWithBooleans);
		assertThat(result).contains("true");
		assertThat(result).contains("false");
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}

	@Test
	@DisplayName("serialize - null 값이 포함된 JSON 직렬화")
	void serialize_NullValues_Success() throws JsonProcessingException {
		// given
		String jsonWithNulls = "{\"field1\":\"value\",\"field2\":null}";

		given(objectMapper.writeValueAsString(insightDetail))
				.willReturn(jsonWithNulls);

		// when
		String result = serializer.serialize(insightDetail);

		// then
		assertThat(result).isEqualTo(jsonWithNulls);
		assertThat(result).contains("null");
		verify(objectMapper, times(1)).writeValueAsString(insightDetail);
	}
}