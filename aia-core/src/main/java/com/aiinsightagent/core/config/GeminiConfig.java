package com.aiinsightagent.core.config;

import com.google.genai.Client;
import com.google.genai.Models;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

		List<Models> modelsList = validModels.stream()
				.map(modelConfig -> Client.builder().apiKey(modelConfig.getApiKey()).build())
				.map(client -> client.models)
				.toList();

		log.info("Created {} Gemini Models instances", modelsList.size());
		return modelsList;
	}
}