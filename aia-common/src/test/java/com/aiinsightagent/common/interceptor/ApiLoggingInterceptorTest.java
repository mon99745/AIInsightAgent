package com.aiinsightagent.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class ApiLoggingInterceptorTest {

	private ApiLoggingInterceptor interceptor;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		interceptor = new ApiLoggingInterceptor();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Nested
	@DisplayName("preHandle 메서드 테스트")
	class PreHandleTest {

		@Test
		@DisplayName("요청 시작 시간이 attribute에 저장되어야 한다")
		void shouldSetStartTimeAttribute() {
			// given
			request.setMethod("GET");
			request.setRequestURI("/api/test");

			// when
			boolean result = interceptor.preHandle(request, response, new Object());

			// then
			assertTrue(result);
			assertNotNull(request.getAttribute("apiStartTime"));
			assertTrue(request.getAttribute("apiStartTime") instanceof Long);
		}

		@Test
		@DisplayName("항상 true를 반환해야 한다")
		void shouldReturnTrue() {
			// given
			request.setMethod("POST");
			request.setRequestURI("/api/insights");

			// when
			boolean result = interceptor.preHandle(request, response, new Object());

			// then
			assertTrue(result);
		}
	}

	@Nested
	@DisplayName("afterCompletion 메서드 테스트")
	class AfterCompletionTest {

		@Test
		@DisplayName("정상 응답 시 응답 시간이 계산되어야 한다")
		void shouldCalculateResponseTime() {
			// given
			request.setMethod("GET");
			request.setRequestURI("/api/test");
			request.setAttribute("apiStartTime", System.currentTimeMillis() - 100);
			response.setStatus(200);

			// when & then
			assertDoesNotThrow(() ->
					interceptor.afterCompletion(request, response, new Object(), null)
			);
		}

		@Test
		@DisplayName("예외 발생 시에도 정상적으로 로깅되어야 한다")
		void shouldLogWhenExceptionOccurs() {
			// given
			request.setMethod("POST");
			request.setRequestURI("/api/insights");
			request.setAttribute("apiStartTime", System.currentTimeMillis() - 50);
			response.setStatus(500);
			Exception exception = new RuntimeException("Test exception");

			// when & then
			assertDoesNotThrow(() ->
					interceptor.afterCompletion(request, response, new Object(), exception)
			);
		}

		@Test
		@DisplayName("시작 시간이 없는 경우에도 예외가 발생하지 않아야 한다")
		void shouldNotThrowWhenStartTimeIsNull() {
			// given
			request.setMethod("GET");
			request.setRequestURI("/api/test");
			response.setStatus(200);

			// when & then
			assertDoesNotThrow(() ->
					interceptor.afterCompletion(request, response, new Object(), null)
			);
		}

		@Test
		@DisplayName("다양한 HTTP 상태 코드를 처리할 수 있어야 한다")
		void shouldHandleVariousStatusCodes() {
			// given
			request.setMethod("DELETE");
			request.setRequestURI("/api/resources/1");
			request.setAttribute("apiStartTime", System.currentTimeMillis() - 200);

			// when & then - 204 No Content
			response.setStatus(204);
			assertDoesNotThrow(() ->
					interceptor.afterCompletion(request, response, new Object(), null)
			);

			// when & then - 404 Not Found
			response.setStatus(404);
			assertDoesNotThrow(() ->
					interceptor.afterCompletion(request, response, new Object(), null)
			);

			// when & then - 401 Unauthorized
			response.setStatus(401);
			assertDoesNotThrow(() ->
					interceptor.afterCompletion(request, response, new Object(), null)
			);
		}
	}

	@Nested
	@DisplayName("전체 요청 흐름 테스트")
	class FullRequestFlowTest {

		@Test
		@DisplayName("preHandle과 afterCompletion이 순차적으로 호출되어야 한다")
		void shouldHandleFullRequestFlow() throws InterruptedException {
			// given
			request.setMethod("GET");
			request.setRequestURI("/api/insights");
			response.setStatus(200);

			// when
			boolean preHandleResult = interceptor.preHandle(request, response, new Object());

			// 약간의 처리 시간 시뮬레이션
			Thread.sleep(10);

			// then
			assertTrue(preHandleResult);
			Long startTime = (Long) request.getAttribute("apiStartTime");
			assertNotNull(startTime);

			// afterCompletion 호출
			assertDoesNotThrow(() ->
					interceptor.afterCompletion(request, response, new Object(), null)
			);
		}
	}
}
