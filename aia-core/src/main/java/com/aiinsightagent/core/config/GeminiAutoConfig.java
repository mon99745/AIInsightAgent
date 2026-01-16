package com.aiinsightagent.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({GeminiProperties.class, RequestQueueProperties.class})
public class GeminiAutoConfig {
}
