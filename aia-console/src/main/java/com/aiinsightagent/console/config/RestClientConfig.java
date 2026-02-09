package com.aiinsightagent.console.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

	private final ConsoleProperties consoleProperties;

	@Bean
	public RestClient actuatorRestClient() {
		return RestClient.builder()
				.baseUrl(consoleProperties.getAppBaseUrl())
				.build();
	}
}
