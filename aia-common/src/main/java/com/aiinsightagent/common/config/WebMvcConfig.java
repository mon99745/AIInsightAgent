package com.aiinsightagent.common.config;

import com.aiinsightagent.common.interceptor.ApiLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "common.api-logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebMvcConfig implements WebMvcConfigurer {

	private final ApiLoggingInterceptor apiLoggingInterceptor;
	private final ApiLoggingProperties apiLoggingProperties;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(apiLoggingInterceptor)
				.addPathPatterns(apiLoggingProperties.getIncludePaths().toArray(new String[0]))
				.excludePathPatterns(apiLoggingProperties.getExcludePaths().toArray(new String[0]));
	}
}
