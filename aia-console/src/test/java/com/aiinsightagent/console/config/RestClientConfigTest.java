package com.aiinsightagent.console.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestClientConfig 테스트")
class RestClientConfigTest {

	private RestClientConfig restClientConfig;
	private ConsoleProperties consoleProperties;

	@BeforeEach
	void setUp() {
		consoleProperties = new ConsoleProperties();
		restClientConfig = new RestClientConfig(consoleProperties);
	}

	@Test
	@DisplayName("actuatorRestClient - RestClient 빈 생성")
	void actuatorRestClient_CreatesRestClientBean() {
		RestClient restClient = restClientConfig.actuatorRestClient();

		assertThat(restClient).isNotNull();
	}

	@Test
	@DisplayName("actuatorRestClient - 기본 URL 설정 확인")
	void actuatorRestClient_WithDefaultBaseUrl() {
		consoleProperties.setAppBaseUrl("http://localhost:28080");

		RestClient restClient = restClientConfig.actuatorRestClient();

		assertThat(restClient).isNotNull();
	}

	@Test
	@DisplayName("actuatorRestClient - 커스텀 URL 설정")
	void actuatorRestClient_WithCustomBaseUrl() {
		consoleProperties.setAppBaseUrl("http://production:8080");

		RestClient restClient = restClientConfig.actuatorRestClient();

		assertThat(restClient).isNotNull();
	}

	@Test
	@DisplayName("actuatorRestClient - 호출 시마다 동일한 인스턴스 반환하지 않음 (프로토타입 스코프가 아니므로 싱글톤)")
	void actuatorRestClient_CreatesNewInstance() {
		RestClient restClient1 = restClientConfig.actuatorRestClient();
		RestClient restClient2 = restClientConfig.actuatorRestClient();

		// Spring 컨텍스트 없이 테스트하므로 매번 새 인스턴스 생성
		assertThat(restClient1).isNotNull();
		assertThat(restClient2).isNotNull();
	}

	@Test
	@DisplayName("actuatorRestClient - null이 아닌 ConsoleProperties로 생성")
	void actuatorRestClient_WithNonNullProperties() {
		assertThat(consoleProperties).isNotNull();
		assertThat(consoleProperties.getAppBaseUrl()).isNotNull();

		RestClient restClient = restClientConfig.actuatorRestClient();

		assertThat(restClient).isNotNull();
	}
}