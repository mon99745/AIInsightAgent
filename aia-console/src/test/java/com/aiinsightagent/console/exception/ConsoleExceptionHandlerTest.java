package com.aiinsightagent.console.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsoleExceptionHandler 테스트")
class ConsoleExceptionHandlerTest {

	@InjectMocks
	private ConsoleExceptionHandler exceptionHandler;

	@Mock
	private HttpServletRequest request;

	private Model model;

	@BeforeEach
	void setUp() {
		model = new ConcurrentModel();
	}

	@Test
	@DisplayName("handleDefaultException - ConsoleError가 있는 예외 처리")
	void handleDefaultException_WithConsoleError() {
		given(request.getRequestURI()).willReturn("/analysis/1");

		ConsoleException ex = new ConsoleException(ConsoleError.ANALYSIS_NOT_FOUND);

		String viewName = exceptionHandler.handleDefaultException(ex, model, request);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(404);
		assertThat(model.getAttribute("error")).isEqualTo("Not Found");
		assertThat(model.getAttribute("path")).isEqualTo("/analysis/1");
	}

	@Test
	@DisplayName("handleDefaultException - INTERNAL_SERVER_ERROR 상태의 에러 처리")
	void handleDefaultException_WithInternalError() {
		given(request.getRequestURI()).willReturn("/system/status");

		ConsoleException ex = new ConsoleException(ConsoleError.ACTUATOR_REQUEST_FAILED);

		String viewName = exceptionHandler.handleDefaultException(ex, model, request);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(500);
		assertThat(model.getAttribute("error")).isEqualTo("Internal Server Error");
	}

	@Test
	@DisplayName("handleGenericException - 일반 예외 처리")
	void handleGenericException_ReturnsErrorView() {
		given(request.getRequestURI()).willReturn("/dashboard");
		given(request.getMethod()).willReturn("GET");

		Exception ex = new RuntimeException("unexpected error");

		String viewName = exceptionHandler.handleGenericException(ex, model, request);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(500);
		assertThat(model.getAttribute("error")).isEqualTo("Internal Server Error");
		assertThat(model.getAttribute("message")).isEqualTo("예상치 못한 오류가 발생했습니다.");
		assertThat(model.getAttribute("path")).isEqualTo("/dashboard");
	}
}
