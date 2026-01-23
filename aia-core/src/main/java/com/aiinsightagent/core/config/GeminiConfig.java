package com.aiinsightagent.core.config;

import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.ListModelsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GeminiConfig {
	protected final GeminiProperties geminiProperties;

	/**
	 * 각 모델 설정별로 Models 인스턴스 생성
	 */
	@Bean
	public List<Models> geminiModelsList() {
		List<GeminiProperties.ModelConfig> validModels = geminiProperties.getValidModels();

		if (validModels.isEmpty()) {
			log.warn("No valid Gemini API keys configured. Gemini features will be disabled.");
			return List.of();
		}

		List<Models> modelsList = createAndValidateModels(validModels);
		log.info("Created {} Gemini Models instances", modelsList.size());
		return modelsList;
	}

	/**
	 * 모델 인스턴스 생성 및 API 키 유효성 검증
	 */
	private List<Models> createAndValidateModels(List<GeminiProperties.ModelConfig> modelConfigs) {
		List<Models> modelsList = new ArrayList<>();
		List<String> failedModels = new ArrayList<>();

		for (GeminiProperties.ModelConfig modelConfig : modelConfigs) {
			try {
				Models models = createModels(modelConfig);
				validateApiKey(models, modelConfig);
				modelsList.add(models);
			} catch (Exception e) {
				log.error("[{}] API key validation failed: {}", modelConfig.getId(), e.getMessage(), e);
				failedModels.add(modelConfig.getId());
			}
		}

		validateAtLeastOneModelAvailable(modelsList, failedModels);
		return modelsList;
	}

	/**
	 * Models 인스턴스 생성
	 */
	protected Models createModels(GeminiProperties.ModelConfig modelConfig) {
		return Client.builder()
				.apiKey(modelConfig.getApiKey())
				.build()
				.models;
	}

	/**
	 * API 키 유효성 검증 - 모델 목록 조회로 확인
	 */
	private void validateApiKey(Models models, GeminiProperties.ModelConfig modelConfig) {
		log.debug("Validating API key for model: {}", modelConfig.getId());
		models.list(ListModelsConfig.builder().pageSize(1).build());
		log.info("Gemini API key validated successfully for model: {} ({})",
				modelConfig.getId(), modelConfig.getName());
	}

	/**
	 * 최소 하나의 유효한 모델이 있는지 검증
	 */
	private void validateAtLeastOneModelAvailable(List<Models> modelsList, List<String> failedModels) {
		if (modelsList.isEmpty()) {
			throw new InsightException(InsightError.INVALID_GEMINI_API_KEY,
					"All Gemini API keys are invalid. Failed models: " + failedModels);
		}

		if (!failedModels.isEmpty()) {
			log.warn("Some Gemini API keys failed validation: {}. Continuing with {} valid keys.",
					failedModels, modelsList.size());
		}
	}
}