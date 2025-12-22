package com.aiinsightagent.core.config;

import com.google.genai.Client;
import com.google.genai.Models;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GeminiConfig {
	protected final GeminiProperties geminiProperties;

	@Bean
	public Client geminiClient() {
		return Client.builder()
				.apiKey(geminiProperties.getApiKey())
				.build();
	}

	@Bean
	public Models geminiModels(Client client) {
		return client.models;
	}
}