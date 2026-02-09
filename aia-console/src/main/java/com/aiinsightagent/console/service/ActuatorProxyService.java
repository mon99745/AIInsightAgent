package com.aiinsightagent.console.service;

import com.aiinsightagent.console.exception.ConsoleError;
import com.aiinsightagent.console.exception.ConsoleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActuatorProxyService {

	private final RestClient actuatorRestClient;

	@SuppressWarnings("unchecked")
	public Map<String, Object> getHealth() {
		try {
			return actuatorRestClient.get()
					.uri("/actuator/health")
					.retrieve()
					.body(Map.class);
		} catch (Exception e) {
			log.warn("Failed to fetch actuator health: {}", e.getMessage());
			return Collections.singletonMap("status", "DOWN");
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMetrics(String metricName) {
		try {
			return actuatorRestClient.get()
					.uri("/actuator/metrics/{name}", metricName)
					.retrieve()
					.body(Map.class);
		} catch (Exception e) {
			log.warn("Failed to fetch actuator metric [{}]: {}", metricName, e.getMessage());
			return Collections.emptyMap();
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getInfo() {
		try {
			return actuatorRestClient.get()
					.uri("/actuator/info")
					.retrieve()
					.body(Map.class);
		} catch (Exception e) {
			log.warn("Failed to fetch actuator info: {}", e.getMessage());
			return Collections.emptyMap();
		}
	}
}
