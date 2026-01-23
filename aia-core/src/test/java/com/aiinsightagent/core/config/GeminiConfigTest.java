package com.aiinsightagent.core.config;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.google.genai.Models;
import com.google.genai.types.ListModelsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiConfigTest {

	@Mock
	private GeminiProperties geminiProperties;

	@Mock
	private Models mockModels;

	private GeminiConfig geminiConfig;

	@BeforeEach
	void setUp() {
		geminiConfig = spy(new GeminiConfig(geminiProperties));
	}

	@Test
	@DisplayName("유효한 API 키가 없으면 빈 리스트 반환")
	void geminiModelsList_noValidModels_returnsEmptyList() {
		// given
		when(geminiProperties.getValidModels()).thenReturn(List.of());

		// when
		List<Models> result = geminiConfig.geminiModelsList();

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("유효한 API 키로 Models 인스턴스 생성 성공")
	void geminiModelsList_validApiKey_returnsModelsList() {
		// given
		GeminiProperties.ModelConfig modelConfig = createModelConfig("m01", "gemini-2.5-flash", "valid-api-key");
		when(geminiProperties.getValidModels()).thenReturn(List.of(modelConfig));
		doReturn(mockModels).when(geminiConfig).createModels(any());

		// when
		List<Models> result = geminiConfig.geminiModelsList();

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(mockModels);
		verify(mockModels).list(any(ListModelsConfig.class));
	}

	@Test
	@DisplayName("여러 API 키 중 일부만 유효하면 유효한 것만 반환")
	void geminiModelsList_someInvalidKeys_returnsOnlyValidModels() {
		// given
		GeminiProperties.ModelConfig validConfig = createModelConfig("m01", "gemini-2.5-flash", "valid-key");
		GeminiProperties.ModelConfig invalidConfig = createModelConfig("m02", "gemini-2.5-flash", "invalid-key");

		Models validModels = mock(Models.class);
		Models invalidModels = mock(Models.class);
		doThrow(new RuntimeException("Invalid API key")).when(invalidModels).list(any(ListModelsConfig.class));

		when(geminiProperties.getValidModels()).thenReturn(List.of(validConfig, invalidConfig));
		doReturn(validModels).when(geminiConfig).createModels(validConfig);
		doReturn(invalidModels).when(geminiConfig).createModels(invalidConfig);

		// when
		List<Models> result = geminiConfig.geminiModelsList();

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(validModels);
	}

	@Test
	@DisplayName("모든 API 키가 유효하지 않으면 InsightException 발생")
	void geminiModelsList_allInvalidKeys_throwsInsightException() {
		// given
		GeminiProperties.ModelConfig invalidConfig1 = createModelConfig("m01", "gemini-2.5-flash", "invalid-key-1");
		GeminiProperties.ModelConfig invalidConfig2 = createModelConfig("m02", "gemini-2.5-flash", "invalid-key-2");

		Models invalidModels1 = mock(Models.class);
		Models invalidModels2 = mock(Models.class);
		doThrow(new RuntimeException("Invalid API key")).when(invalidModels1).list(any(ListModelsConfig.class));
		doThrow(new RuntimeException("Invalid API key")).when(invalidModels2).list(any(ListModelsConfig.class));

		when(geminiProperties.getValidModels()).thenReturn(List.of(invalidConfig1, invalidConfig2));
		doReturn(invalidModels1).when(geminiConfig).createModels(invalidConfig1);
		doReturn(invalidModels2).when(geminiConfig).createModels(invalidConfig2);

		// when & then
		assertThatThrownBy(() -> geminiConfig.geminiModelsList())
				.isInstanceOf(InsightException.class)
				.satisfies(ex -> {
					InsightException insightException = (InsightException) ex;
					assertThat(insightException.getError()).isEqualTo(InsightError.INVALID_GEMINI_API_KEY);
				})
				.hasMessageContaining("m01")
				.hasMessageContaining("m02");
	}

	@Test
	@DisplayName("Models 생성 중 예외 발생해도 다른 모델 검증 계속 진행")
	void geminiModelsList_createModelsFails_continuesWithOthers() {
		// given
		GeminiProperties.ModelConfig failConfig = createModelConfig("m01", "gemini-2.5-flash", "fail-key");
		GeminiProperties.ModelConfig validConfig = createModelConfig("m02", "gemini-2.5-flash", "valid-key");

		Models validModels = mock(Models.class);

		when(geminiProperties.getValidModels()).thenReturn(List.of(failConfig, validConfig));
		doThrow(new RuntimeException("Connection failed")).when(geminiConfig).createModels(failConfig);
		doReturn(validModels).when(geminiConfig).createModels(validConfig);

		// when
		List<Models> result = geminiConfig.geminiModelsList();

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(validModels);
	}

	private GeminiProperties.ModelConfig createModelConfig(String id, String name, String apiKey) {
		GeminiProperties.ModelConfig config = new GeminiProperties.ModelConfig();
		config.setId(id);
		config.setName(name);
		config.setApiKey(apiKey);
		return config;
	}
}
