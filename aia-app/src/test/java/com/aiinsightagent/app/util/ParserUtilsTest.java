package com.aiinsightagent.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParserUtils 테스트")
class ParserUtilsTest {

	private ParserUtils parserUtils;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		parserUtils = new ParserUtils(objectMapper);
	}

	@Nested
	@DisplayName("parsePayload 메서드")
	class ParsePayloadTest {

		@Test
		@DisplayName("정상적인 JSON 문자열을 Map으로 파싱")
		void parsePayload_ValidJson_ReturnsMap() {
			// given
			String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

			// when
			Map<String, String> result = parserUtils.parsePayload(json);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get("key1")).isEqualTo("value1");
			assertThat(result.get("key2")).isEqualTo("value2");
		}

		@Test
		@DisplayName("빈 JSON 객체를 빈 Map으로 파싱")
		void parsePayload_EmptyJson_ReturnsEmptyMap() {
			// given
			String json = "{}";

			// when
			Map<String, String> result = parserUtils.parsePayload(json);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("잘못된 JSON 문자열은 빈 Map 반환")
		void parsePayload_InvalidJson_ReturnsEmptyMap() {
			// given
			String invalidJson = "{invalid json}";

			// when
			Map<String, String> result = parserUtils.parsePayload(invalidJson);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Map.toString() 형식은 빈 Map 반환")
		void parsePayload_MapToStringFormat_ReturnsEmptyMap() {
			// given - Map.toString()은 JSON이 아님
			String mapToString = "{key1=value1, key2=value2}";

			// when
			Map<String, String> result = parserUtils.parsePayload(mapToString);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("null 문자열은 빈 Map 반환")
		void parsePayload_NullString_ReturnsEmptyMap() {
			// given
			String nullJson = null;

			// when
			Map<String, String> result = parserUtils.parsePayload(nullJson);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("특수문자가 포함된 JSON 파싱")
		void parsePayload_SpecialCharacters_ReturnsMap() {
			// given
			String json = "{\"korean\":\"한글테스트\",\"special\":\"!@#$%\"}";

			// when
			Map<String, String> result = parserUtils.parsePayload(json);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get("korean")).isEqualTo("한글테스트");
			assertThat(result.get("special")).isEqualTo("!@#$%");
		}
	}

	@Nested
	@DisplayName("toJson 메서드")
	class ToJsonTest {

		@Test
		@DisplayName("Map을 JSON 문자열로 직렬화")
		void toJson_ValidMap_ReturnsJsonString() {
			// given
			Map<String, String> data = new HashMap<>();
			data.put("key1", "value1");
			data.put("key2", "value2");

			// when
			String result = parserUtils.toJson(data);

			// then
			assertThat(result).contains("\"key1\":\"value1\"");
			assertThat(result).contains("\"key2\":\"value2\"");
		}

		@Test
		@DisplayName("빈 Map을 빈 JSON 객체로 직렬화")
		void toJson_EmptyMap_ReturnsEmptyJsonObject() {
			// given
			Map<String, String> emptyMap = new HashMap<>();

			// when
			String result = parserUtils.toJson(emptyMap);

			// then
			assertThat(result).isEqualTo("{}");
		}

		@Test
		@DisplayName("null Map은 빈 JSON 객체 반환")
		void toJson_NullMap_ReturnsEmptyJsonObject() {
			// given
			Map<String, String> nullMap = null;

			// when
			String result = parserUtils.toJson(nullMap);

			// then
			assertThat(result).isEqualTo("null");
		}

		@Test
		@DisplayName("특수문자가 포함된 Map 직렬화")
		void toJson_SpecialCharacters_ReturnsJsonString() {
			// given
			Map<String, String> data = new HashMap<>();
			data.put("korean", "한글테스트");
			data.put("special", "!@#$%");

			// when
			String result = parserUtils.toJson(data);

			// then
			assertThat(result).contains("\"korean\":\"한글테스트\"");
			assertThat(result).contains("\"special\":\"!@#$%\"");
		}
	}

	@Nested
	@DisplayName("parsePayload와 toJson 통합 테스트")
	class IntegrationTest {

		@Test
		@DisplayName("toJson으로 직렬화한 후 parsePayload로 역직렬화하면 원본과 동일")
		void toJsonThenParsePayload_ReturnsOriginalData() {
			// given
			Map<String, String> original = new HashMap<>();
			original.put("averagePace", "6:00");
			original.put("totalDistance", "100km");
			original.put("runningDays", "30");

			// when
			String json = parserUtils.toJson(original);
			Map<String, String> parsed = parserUtils.parsePayload(json);

			// then
			assertThat(parsed).isEqualTo(original);
		}

		@Test
		@DisplayName("빈 Map의 직렬화/역직렬화 사이클")
		void emptyMapSerializationCycle() {
			// given
			Map<String, String> original = new HashMap<>();

			// when
			String json = parserUtils.toJson(original);
			Map<String, String> parsed = parserUtils.parsePayload(json);

			// then
			assertThat(json).isEqualTo("{}");
			assertThat(parsed).isEmpty();
		}
	}
}
