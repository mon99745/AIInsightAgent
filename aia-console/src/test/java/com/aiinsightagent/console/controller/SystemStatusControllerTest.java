package com.aiinsightagent.console.controller;

import com.aiinsightagent.console.service.ActuatorProxyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemStatusController 테스트")
class SystemStatusControllerTest {

	@Mock
	private ActuatorProxyService actuatorProxyService;

	@InjectMocks
	private SystemStatusController systemStatusController;

	@Test
	@DisplayName("status - 시스템 상태 페이지 반환")
	void status_ReturnsStatusView() {
		Map<String, Object> health = Map.of("status", "UP");
		Map<String, Object> info = Map.of("app", Map.of("name", "aia-app"));

		given(actuatorProxyService.getHealth()).willReturn(health);
		given(actuatorProxyService.getInfo()).willReturn(info);

		Model model = new ConcurrentModel();
		String viewName = systemStatusController.status(model);

		assertThat(viewName).isEqualTo("system/status");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("System Status");
		assertThat(model.getAttribute("health")).isEqualTo(health);
		assertThat(model.getAttribute("info")).isEqualTo(info);
	}

	@Test
	@DisplayName("status - 서비스 다운 시에도 정상 반환")
	void status_WhenServiceDown_StillReturnsView() {
		Map<String, Object> health = Collections.singletonMap("status", "DOWN");
		Map<String, Object> info = Collections.emptyMap();

		given(actuatorProxyService.getHealth()).willReturn(health);
		given(actuatorProxyService.getInfo()).willReturn(info);

		Model model = new ConcurrentModel();
		String viewName = systemStatusController.status(model);

		assertThat(viewName).isEqualTo("system/status");
		assertThat(model.getAttribute("health")).isEqualTo(health);
	}
}
