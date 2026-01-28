package com.aiinsightagent.common.exception;

import com.aiinsightagent.common.filter.TraceIdFilter;
import com.google.genai.errors.ClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CommonExceptionHandlerTest {

	@InjectMocks
	private CommonExceptionHandler handler;

	private MockHttpServletRequest request;

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		request.setRequestURI("/api/test");
		MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, "test-trace-id");
	}

	@AfterEach
	void tearDown() {
		MDC.clear();
	}

	@Nested
	@DisplayName("handleDefaultException 테스트")
	class HandleDefaultExceptionTest {

		@Test
		@DisplayName("DefaultException 처리 시 에러 코드와 메시지를 반환한다")
		void handleDefaultException_withError_returnsErrorResponse() {
			// given
			DefaultException ex = new CommonException(CommonError.COM_INVALID_ARGUMENT, "잘못된 인자");

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleDefaultException(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo(CommonError.COM_INVALID_ARGUMENT.getCode());
			assertThat(response.getBody().get("path")).isEqualTo("/api/test");
			assertThat(response.getBody().get("traceId")).isEqualTo("test-trace-id");
		}

		@Test
		@DisplayName("기본 Error를 가진 DefaultException 처리 시 기본 코드를 반환한다")
		void handleDefaultException_defaultError_returnsDefaultCode() {
			// given
			DefaultException ex = new CommonException("에러 메시지");

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleDefaultException(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo(Error.DefaultError.NONE.getCode());
		}
	}

	@Nested
	@DisplayName("handleMissingParams 테스트")
	class HandleMissingParamsTest {

		@Test
		@DisplayName("필수 파라미터 누락 시 400 응답을 반환한다")
		void handleMissingParams_returnsBadRequest() {
			// given
			MissingServletRequestParameterException ex =
					new MissingServletRequestParameterException("userId", "String");

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleMissingParams(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo("MISSING_PARAMETER");
			assertThat(response.getBody().get("message")).asString().contains("userId");
		}
	}

	@Nested
	@DisplayName("handleInvalidJson 테스트")
	class HandleInvalidJsonTest {

		@Test
		@DisplayName("잘못된 JSON 요청 시 400 응답을 반환한다")
		void handleInvalidJson_returnsBadRequest() {
			// given
			HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
					"JSON parse error",
					new MockHttpInputMessage("invalid".getBytes())
			);

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleInvalidJson(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo("INVALID_JSON");
			assertThat(response.getBody().get("message")).isEqualTo("Request body is not valid JSON");
		}
	}

	@Nested
	@DisplayName("handleNotFound 테스트")
	class HandleNotFoundTest {

		@Test
		@DisplayName("리소스를 찾을 수 없을 때 404 응답을 반환한다")
		void handleNotFound_returnsNotFound() throws NoResourceFoundException {
			// given
			NoResourceFoundException ex = new NoResourceFoundException(
					org.springframework.http.HttpMethod.GET,
					"/api/unknown"
			);

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo("NOT_FOUND");
			assertThat(response.getBody().get("message")).asString().contains("/api/test");
		}
	}

	@Nested
	@DisplayName("handleMethodNotAllowed 테스트")
	class HandleMethodNotAllowedTest {

		@Test
		@DisplayName("지원하지 않는 HTTP 메서드 요청 시 405 응답을 반환한다")
		void handleMethodNotAllowed_returnsMethodNotAllowed() {
			// given
			HttpRequestMethodNotSupportedException ex =
					new HttpRequestMethodNotSupportedException("DELETE");

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleMethodNotAllowed(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo("METHOD_NOT_ALLOWED");
			assertThat(response.getBody().get("message")).asString().contains("DELETE");
		}
	}

	@Nested
	@DisplayName("handleUnsupportedMediaType 테스트")
	class HandleUnsupportedMediaTypeTest {

		@Test
		@DisplayName("지원하지 않는 Content-Type 요청 시 415 응답을 반환한다")
		void handleUnsupportedMediaType_returnsUnsupportedMediaType() {
			// given
			HttpMediaTypeNotSupportedException ex =
					new HttpMediaTypeNotSupportedException("Unsupported media type: text/plain");

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleUnsupportedMediaType(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo("UNSUPPORTED_MEDIA_TYPE");
		}
	}

	@Nested
	@DisplayName("handleClientException 테스트")
	class HandleClientExceptionTest {

		@Test
		@DisplayName("Gemini API 클라이언트 에러 시 429 응답을 반환한다")
		void handleClientException_returnsTooManyRequests() {
			// given
			ClientException ex = new ClientException(429, "Rate limit exceeded", "RATE_LIMIT_EXCEEDED");

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleClientException(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo("RATE_LIMIT_EXCEEDED");
			assertThat(response.getBody().get("message")).asString().contains("rate limit");
		}
	}

	@Nested
	@DisplayName("handleGenericException 테스트")
	class HandleGenericExceptionTest {

		@Test
		@DisplayName("일반 예외 발생 시 500 응답을 반환한다")
		void handleGenericException_returnsInternalServerError() {
			// given
			Exception ex = new RuntimeException("예기치 않은 오류");

			// when
			ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex, request);

			// then
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().get("code")).isEqualTo("INTERNAL_ERROR");
			assertThat(response.getBody().get("message")).asString().contains("unexpected error");
		}
	}
}
