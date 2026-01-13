package com.aiinsightagent.common.exception;

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
import java.util.Map;

/**
 * 공통 모듈용 글로벌 예외 처리
 */
@RestControllerAdvice
public class CommonExceptionHandler {

	// HTTP 405 - Method Not Allowed
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(
			HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {

		Map<String, Object> body = Map.of(
				"code", "METHOD_NOT_ALLOWED",
				"message", "Method '" + ex.getMethod() + "' is not supported for this endpoint",
				"path", request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
	}

	// HTTP 404 - Not Found
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(
			NoResourceFoundException ex,
			HttpServletRequest request) {

		Map<String, Object> body = Map.of(
				"code", "NOT_FOUND",
				"message", "Resource not found: " + request.getRequestURI(),
				"path", request.getRequestURI()
		);

		return ResponseEntity.status(404).body(body);
	}

	// HTTP 415 - Unsupported Media Type
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(
			HttpMediaTypeNotSupportedException ex,
			HttpServletRequest request) {

		Map<String, Object> body = Map.of(
				"code", "UNSUPPORTED_MEDIA_TYPE",
				"message", "Content-Type '" + ex.getContentType() + "' is not supported",
				"path", request.getRequestURI()
		);

		return ResponseEntity.status(415).body(body);
	}

	// 공통 서브 모듈 예외 처리
	@ExceptionHandler(DefaultException.class)
	public ResponseEntity<Map<String, Object>> handleDefaultException(DefaultException ex,
																	  HttpServletRequest request) {

		Map<String, Object> body = Map.of(
				"code", ex.getError() != null ? ex.getError().getCode() : "DEF-UNKNOWN",
				"message", ex.getMessage(),
				"path", request.getRequestURI()
		);

		// 상태코드는 Error 객체에서 가져오도록 구현 가능, 없으면 500
		return ResponseEntity.status(500).body(body);
	}

	// 필수 파라미터 누락 처리
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> handleMissingParams(
			MissingServletRequestParameterException ex,
			HttpServletRequest request) {

		Map<String, Object> body = Map.of(
				"code", "MISSING_PARAMETER",
				"message", "Required parameter '" + ex.getParameterName() + "' is missing",
				"path", request.getRequestURI()
		);

		return ResponseEntity.status(400).body(body);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidJson(
			HttpMessageNotReadableException ex,
			HttpServletRequest request) {
		Map<String, Object> body = Map.of(
				"code", "INVALID_JSON",
				"message", "Request body is not valid JSON: " + ex.getMostSpecificCause().getMessage(),
				"path", request.getRequestURI()
		);
		return ResponseEntity.status(400).body(body);
	}

	// 기타 일반 예외 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex,
																	  HttpServletRequest request) {

		Map<String, Object> body = Map.of(
				"code", "INTERNAL_ERROR",
				"message", ex.getMessage(),
				"path", request.getRequestURI()
		);

		return ResponseEntity.status(500).body(body);
	}
}