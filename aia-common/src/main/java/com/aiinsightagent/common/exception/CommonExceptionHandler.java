package com.aiinsightagent.common.exception;

import com.aiinsightagent.common.filter.TraceIdHolder;
import com.google.genai.errors.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 공통 모듈용 글로벌 예외 처리
 */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

	// 공통 서브 모듈 예외 처리
	@ExceptionHandler(DefaultException.class)
	public ResponseEntity<Map<String, Object>> handleDefaultException(DefaultException ex,
																	  HttpServletRequest request) {

		String code = ex.getError() != null ? ex.getError().getCode() : "DEF-UNKNOWN";
		Map<String, Object> body = buildErrorBody(code, ex.getMessage(), request.getRequestURI());

		HttpStatus status = ex.getError() != null ? ex.getError().getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(status).body(body);
	}

	// 400 - MISSING_PARAMETER
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> handleMissingParams(
			MissingServletRequestParameterException ex,
			HttpServletRequest request) {

		Map<String, Object> body = buildErrorBody(
				"MISSING_PARAMETER",
				"Required parameter '" + ex.getParameterName() + "' is missing",
				request.getRequestURI()
		);

		return ResponseEntity.status(400).body(body);
	}

	// 400 - INVALID_JSON
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidJson(
			HttpMessageNotReadableException ex,
			HttpServletRequest request) {

		log.warn("Invalid JSON request: {}", ex.getMessage());

		Map<String, Object> body = buildErrorBody(
				"INVALID_JSON",
				"Request body is not valid JSON",
				request.getRequestURI()
		);

		return ResponseEntity.status(400).body(body);
	}

	// HTTP 404 - Not Found
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(
			NoResourceFoundException ex,
			HttpServletRequest request) {

		Map<String, Object> body = buildErrorBody(
				"NOT_FOUND",
				"Resource not found: " + request.getRequestURI(),
				request.getRequestURI()
		);

		return ResponseEntity.status(404).body(body);
	}

	// HTTP 405 - Method Not Allowed
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(
			HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {

		Map<String, Object> body = buildErrorBody(
				"METHOD_NOT_ALLOWED",
				"Method '" + ex.getMethod() + "' is not supported for this endpoint",
				request.getRequestURI()
		);

		return ResponseEntity.status(405).body(body);
	}

	// HTTP 415 - Unsupported Media Type
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(
			HttpMediaTypeNotSupportedException ex,
			HttpServletRequest request) {

		Map<String, Object> body = buildErrorBody(
				"UNSUPPORTED_MEDIA_TYPE",
				"Content-Type '" + ex.getContentType() + "' is not supported",
				request.getRequestURI()
		);

		return ResponseEntity.status(415).body(body);
	}

	// 429 - Too Many Requests
	@ExceptionHandler(ClientException.class)
	public ResponseEntity<Map<String, Object>> handleClientException(
			ClientException ex,
			HttpServletRequest request) {

		log.warn("Gemini API client error: code={}, message={}", ex.code(), ex.getMessage());

		Map<String, Object> body = buildErrorBody(
				"RATE_LIMIT_EXCEEDED",
				"API rate limit exceeded. Please try again later.",
				request.getRequestURI()
		);

		return ResponseEntity.status(429).body(body);
	}

	// etc
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex,
																	  HttpServletRequest request) {

		log.error("Unexpected error occurred: {} {}", request.getMethod(), request.getRequestURI(), ex);

		Map<String, Object> body = buildErrorBody(
				"INTERNAL_ERROR",
				"An unexpected error occurred. Please contact support with traceId.",
				request.getRequestURI()
		);

		return ResponseEntity.status(500).body(body);
	}

	private Map<String, Object> buildErrorBody(String code, String message, String path) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("traceId", TraceIdHolder.getTraceId());
		body.put("code", code);
		body.put("message", message);
		body.put("path", path);
		return body;
	}
}