package com.aiinsightagent.console.controller;

import com.aiinsightagent.common.filter.TraceIdHolder;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsoleErrorController 테스트")
class ConsoleErrorControllerTest {

	@InjectMocks
	private ConsoleErrorController consoleErrorController;

	@Mock
	private HttpServletRequest request;

	private Model model;
	private MockedStatic<TraceIdHolder> traceIdHolderMock;

	@BeforeEach
	void setUp() {
		model = new ConcurrentModel();
		traceIdHolderMock = mockStatic(TraceIdHolder.class);
		traceIdHolderMock.when(TraceIdHolder::getTraceId).thenReturn("test-trace-id");
	}

	@AfterEach
	void tearDown() {
		traceIdHolderMock.close();
	}

	@Test
	@DisplayName("handleError - 404 에러 처리")
	void handleError_With404Status() {
		given(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).willReturn(404);
		given(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).willReturn("Not Found");
		given(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).willReturn("/test/path");

		String viewName = consoleErrorController.handleError(request, model);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(404);
		assertThat(model.getAttribute("error")).isEqualTo("Not Found");
		assertThat(model.getAttribute("message")).isEqualTo("Not Found");
		assertThat(model.getAttribute("traceId")).isEqualTo("test-trace-id");
		assertThat(model.getAttribute("path")).isEqualTo("/test/path");
	}

	@Test
	@DisplayName("handleError - 500 에러 처리")
	void handleError_With500Status() {
		given(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).willReturn(500);
		given(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).willReturn("Internal Server Error");
		given(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).willReturn("/error/path");

		String viewName = consoleErrorController.handleError(request, model);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(500);
		assertThat(model.getAttribute("error")).isEqualTo("Internal Server Error");
		assertThat(model.getAttribute("message")).isEqualTo("Internal Server Error");
	}

	@Test
	@DisplayName("handleError - statusCode가 null일 때 기본값 500")
	void handleError_WithNullStatus_Returns500() {
		given(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).willReturn(null);
		given(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).willReturn(null);
		given(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).willReturn("/path");

		String viewName = consoleErrorController.handleError(request, model);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(500);
		assertThat(model.getAttribute("error")).isEqualTo("Internal Server Error");
	}

	@Test
	@DisplayName("handleError - message가 null일 때 기본 메시지")
	void handleError_WithNullMessage_ReturnsDefaultMessage() {
		given(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).willReturn(403);
		given(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).willReturn(null);
		given(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).willReturn("/forbidden");

		String viewName = consoleErrorController.handleError(request, model);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(403);
		assertThat(model.getAttribute("message")).isEqualTo("요청을 처리할 수 없습니다.");
	}

	@Test
	@DisplayName("handleError - message가 빈 문자열일 때 기본 메시지")
	void handleError_WithEmptyMessage_ReturnsDefaultMessage() {
		given(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).willReturn(400);
		given(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).willReturn("");
		given(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).willReturn("/bad-request");

		String viewName = consoleErrorController.handleError(request, model);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("message")).isEqualTo("요청을 처리할 수 없습니다.");
	}

	@Test
	@DisplayName("handleError - 알 수 없는 HTTP 상태 코드 처리")
	void handleError_WithUnknownStatusCode_ReturnsUnknownError() {
		given(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).willReturn(999);
		given(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).willReturn("Custom error");
		given(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).willReturn("/custom");

		String viewName = consoleErrorController.handleError(request, model);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isEqualTo(999);
		assertThat(model.getAttribute("error")).isEqualTo("Unknown Error");
	}

	@Test
	@DisplayName("handleError - 모든 속성이 정상적으로 설정됨")
	void handleError_SetsAllAttributes() {
		given(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).willReturn(401);
		given(request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).willReturn("Unauthorized");
		given(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).willReturn("/secure/path");

		String viewName = consoleErrorController.handleError(request, model);

		assertThat(viewName).isEqualTo("error/error");
		assertThat(model.getAttribute("status")).isNotNull();
		assertThat(model.getAttribute("error")).isNotNull();
		assertThat(model.getAttribute("message")).isNotNull();
		assertThat(model.getAttribute("traceId")).isNotNull();
		assertThat(model.getAttribute("path")).isNotNull();
	}
}