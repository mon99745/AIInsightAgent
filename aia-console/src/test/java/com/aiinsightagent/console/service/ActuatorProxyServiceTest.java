package com.aiinsightagent.console.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActuatorProxyService 테스트")
class ActuatorProxyServiceTest {

	@Mock
	private RestClient actuatorRestClient;

	@Mock
	private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

	@Mock
	private RestClient.RequestHeadersSpec requestHeadersSpec;

	@Mock
	private RestClient.ResponseSpec responseSpec;

	private ActuatorProxyService actuatorProxyService;

	@BeforeEach
	void setUp() {
		actuatorProxyService = new ActuatorProxyService(actuatorRestClient);
	}

	@Test
	@DisplayName("getHealth - 정상 응답 반환")
	void getHealth_ReturnsHealthMap() {
		Map<String, Object> healthData = Map.of("status", "UP");

		given(actuatorRestClient.get()).willReturn(requestHeadersUriSpec);
		given(requestHeadersUriSpec.uri("/actuator/health")).willReturn(requestHeadersSpec);
		given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.body(Map.class)).willReturn(healthData);

		Map<String, Object> result = actuatorProxyService.getHealth();

		assertThat(result).containsEntry("status", "UP");
	}

	@Test
	@DisplayName("getHealth - 예외 발생 시 DOWN 상태 반환")
	void getHealth_OnException_ReturnsDown() {
		given(actuatorRestClient.get()).willThrow(new RestClientException("connection refused"));

		Map<String, Object> result = actuatorProxyService.getHealth();

		assertThat(result).containsEntry("status", "DOWN");
	}

	@Test
	@DisplayName("getMetrics - 정상 응답 반환")
	void getMetrics_ReturnsMetricMap() {
		Map<String, Object> metricData = Map.of("name", "jvm.memory.used", "measurements", "value");

		given(actuatorRestClient.get()).willReturn(requestHeadersUriSpec);
		given(requestHeadersUriSpec.uri(eq("/actuator/metrics/{name}"), eq("jvm.memory.used")))
				.willReturn(requestHeadersSpec);
		given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.body(Map.class)).willReturn(metricData);

		Map<String, Object> result = actuatorProxyService.getMetrics("jvm.memory.used");

		assertThat(result).containsEntry("name", "jvm.memory.used");
	}

	@Test
	@DisplayName("getMetrics - 예외 발생 시 빈 Map 반환")
	void getMetrics_OnException_ReturnsEmptyMap() {
		given(actuatorRestClient.get()).willThrow(new RestClientException("timeout"));

		Map<String, Object> result = actuatorProxyService.getMetrics("jvm.memory.used");

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getInfo - 정상 응답 반환")
	void getInfo_ReturnsInfoMap() {
		Map<String, Object> infoData = Map.of("app", Map.of("name", "aia-app"));

		given(actuatorRestClient.get()).willReturn(requestHeadersUriSpec);
		given(requestHeadersUriSpec.uri("/actuator/info")).willReturn(requestHeadersSpec);
		given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.body(Map.class)).willReturn(infoData);

		Map<String, Object> result = actuatorProxyService.getInfo();

		assertThat(result).containsKey("app");
	}

	@Test
	@DisplayName("getInfo - 예외 발생 시 빈 Map 반환")
	void getInfo_OnException_ReturnsEmptyMap() {
		given(actuatorRestClient.get()).willThrow(new RestClientException("error"));

		Map<String, Object> result = actuatorProxyService.getInfo();

		assertThat(result).isEmpty();
	}
}
