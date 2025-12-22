package com.aiinsightagent.core.config;

import com.aiinsightagent.core.adapter.GeminiChatAdapter;
import com.google.genai.Models;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiAutoConfig {
	@Bean
	@ConditionalOnMissingBean(GeminiChatAdapter.class)
	public GeminiChatAdapter geminiChatService(GeminiProperties geminiProperties, Models models) {
		return new GeminiChatAdapter(geminiProperties, models);
	}
}