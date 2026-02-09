package com.aiinsightagent.console.exception;

import com.aiinsightagent.common.exception.DefaultException;
import com.aiinsightagent.common.filter.TraceIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConsoleExceptionHandler {

	@ExceptionHandler(DefaultException.class)
	public String handleDefaultException(DefaultException ex, Model model, HttpServletRequest request) {
		HttpStatus status = ex.getError() != null ? ex.getError().getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
		String code = ex.getError() != null ? ex.getError().getCode() : "UNKNOWN";

		log.warn("Console error: code={}, message={}, path={}", code, ex.getMessage(), request.getRequestURI());

		model.addAttribute("status", status.value());
		model.addAttribute("error", status.getReasonPhrase());
		model.addAttribute("message", ex.getMessage());
		model.addAttribute("traceId", TraceIdHolder.getTraceId());
		model.addAttribute("path", request.getRequestURI());

		return "error/error";
	}

	@ExceptionHandler(Exception.class)
	public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
		log.error("Unexpected console error: {} {}", request.getMethod(), request.getRequestURI(), ex);

		model.addAttribute("status", 500);
		model.addAttribute("error", "Internal Server Error");
		model.addAttribute("message", "예상치 못한 오류가 발생했습니다.");
		model.addAttribute("traceId", TraceIdHolder.getTraceId());
		model.addAttribute("path", request.getRequestURI());

		return "error/error";
	}
}
