package com.aiinsightagent.core.config;

import com.aiinsightagent.core.service.GeminiChatService;
import com.google.genai.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiAutoConfig {
	@Bean
	@ConditionalOnMissingBean(GeminiChatService.class)
	public GeminiChatService geminiChatService(GeminiProperties geminiProperties, Client geminiClient) {
		return new GeminiChatService(geminiProperties, geminiClient);
	}
}