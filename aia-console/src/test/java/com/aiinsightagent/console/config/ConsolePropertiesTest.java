package com.aiinsightagent.console.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleProperties 테스트")
class ConsolePropertiesTest {

	@Test
	@DisplayName("기본값 검증 - appBaseUrl")
	void defaultValue_AppBaseUrl() {
		ConsoleProperties properties = new ConsoleProperties();

		assertThat(properties.getAppBaseUrl()).isEqualTo("http://localhost:28080");
	}

	@Test
	@DisplayName("setter로 값 변경")
	void setter_ChangesValue() {
		ConsoleProperties properties = new ConsoleProperties();
		properties.setAppBaseUrl("http://prod-server:8080");

		assertThat(properties.getAppBaseUrl()).isEqualTo("http://prod-server:8080");
	}
}
