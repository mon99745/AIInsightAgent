package com.aiinsightagent.core.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiPropertiesTest {

	private GeminiProperties createGeminiProperties() throws Exception {
		Constructor<GeminiProperties> constructor = GeminiProperties.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}

	private GeminiProperties.ModelConfig createModelConfig(String id, String name, String apiKey) {
		GeminiProperties.ModelConfig config = new GeminiProperties.ModelConfig();
		config.setId(id);
		config.setName(name);
		config.setApiKey(apiKey);
		return config;
	}

	@Test
	@DisplayName("getValidModels - 빈 값과 null 필터링 성공")
	void getValidModels_filtersEmptyAndNull() throws Exception {

		// given
		GeminiProperties properties = createGeminiProperties();
		properties.setModels(Arrays.asList(
				createModelConfig("m00", "gemini-2.5-flash", "key1"),
				createModelConfig("m01", "gemini-2.5-flash", ""),
				createModelConfig("m02", "gemini-2.5-flash", "key2"),
				createModelConfig("m03", "gemini-2.5-flash", null),
				createModelConfig("m04", "gemini-2.5-flash", "   "),
				createModelConfig("m05", "gemini-2.5-flash", "key3")
		));

		// when
		List<GeminiProperties.ModelConfig> validModels = properties.getValidModels();

		// then
		assertEquals(3, validModels.size());
		assertEquals("key1", validModels.get(0).getApiKey());
		assertEquals("key2", validModels.get(1).getApiKey());
		assertEquals("key3", validModels.get(2).getApiKey());
	}

	@Test
	@DisplayName("getValidModels - 모든 모델이 유효한 경우")
	void getValidModels_allValid() throws Exception {

		// given
		GeminiProperties properties = createGeminiProperties();
		properties.setModels(Arrays.asList(
				createModelConfig("m00", "gemini-2.5-flash", "key0"),
				createModelConfig("m01", "gemini-2.5-flash", "key1"),
				createModelConfig("m02", "gemini-2.5-flash", "key2"),
				createModelConfig("m03", "gemini-2.5-flash", "key3"),
				createModelConfig("m04", "gemini-2.5-flash", "key4")
		));

		// when
		List<GeminiProperties.ModelConfig> validModels = properties.getValidModels();

		// then
		assertEquals(5, validModels.size());
	}

	@Test
	@DisplayName("getValidModels - 빈 리스트인 경우")
	void getValidModels_emptyList() throws Exception {

		// given
		GeminiProperties properties = createGeminiProperties();

		// when
		List<GeminiProperties.ModelConfig> validModels = properties.getValidModels();

		// then
		assertTrue(validModels.isEmpty());
	}

	@Test
	@DisplayName("getApiKey - 첫 번째 유효한 키 반환")
	void getApiKey_returnsFirstValidKey() throws Exception {

		// given
		GeminiProperties properties = createGeminiProperties();
		properties.setModels(Arrays.asList(
				createModelConfig("m00", "gemini-2.5-flash", ""),
				createModelConfig("m01", "gemini-2.5-flash", "first-valid-key"),
				createModelConfig("m02", "gemini-2.5-flash", "second-key")
		));

		// when
		String apiKey = properties.getApiKey();

		// then
		assertEquals("first-valid-key", apiKey);
	}

	@Test
	@DisplayName("getApiKey - 유효한 키가 없으면 null 반환")
	void getApiKey_returnsNullWhenNoValidKeys() throws Exception {

		// given
		GeminiProperties properties = createGeminiProperties();
		properties.setModels(Arrays.asList(
				createModelConfig("m00", "gemini-2.5-flash", ""),
				createModelConfig("m01", "gemini-2.5-flash", null),
				createModelConfig("m02", "gemini-2.5-flash", "   ")
		));

		// when
		String apiKey = properties.getApiKey();

		// then
		assertNull(apiKey);
	}

	@Test
	@DisplayName("10개 모델 설정 및 조회 성공")
	void getValidModels_tenModels() throws Exception {

		// given
		GeminiProperties properties = createGeminiProperties();
		properties.setModels(Arrays.asList(
				createModelConfig("m00", "gemini-2.5-flash", "key0"),
				createModelConfig("m01", "gemini-2.5-flash", "key1"),
				createModelConfig("m02", "gemini-2.5-flash", "key2"),
				createModelConfig("m03", "gemini-2.5-flash", "key3"),
				createModelConfig("m04", "gemini-2.5-flash", "key4"),
				createModelConfig("m05", "gemini-2.5-flash", "key5"),
				createModelConfig("m06", "gemini-2.5-flash", "key6"),
				createModelConfig("m07", "gemini-2.5-flash", "key7"),
				createModelConfig("m08", "gemini-2.5-flash", "key8"),
				createModelConfig("m09", "gemini-2.5-flash", "key9")
		));

		// when
		List<GeminiProperties.ModelConfig> validModels = properties.getValidModels();

		// then
		assertEquals(10, validModels.size());
		for (int i = 0; i < 10; i++) {
			assertEquals("key" + i, validModels.get(i).getApiKey());
			assertEquals("m0" + i, validModels.get(i).getId());
		}
	}

	@Test
	@DisplayName("ModelConfig - id, name, apiKey 설정 및 조회")
	void modelConfig_gettersAndSetters() {

		// given
		GeminiProperties.ModelConfig config = new GeminiProperties.ModelConfig();

		// when
		config.setId("test-id");
		config.setName("gemini-2.5-flash");
		config.setApiKey("test-api-key");

		// then
		assertEquals("test-id", config.getId());
		assertEquals("gemini-2.5-flash", config.getName());
		assertEquals("test-api-key", config.getApiKey());
	}
}
