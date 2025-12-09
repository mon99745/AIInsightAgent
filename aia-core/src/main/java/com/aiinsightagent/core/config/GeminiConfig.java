package com.aiinsightagent.core.config;

import com.google.genai.Client;
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
}