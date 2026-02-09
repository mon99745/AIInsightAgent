package com.aiinsightagent.console.controller;

import com.aiinsightagent.common.filter.TraceIdHolder;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ConsoleErrorController implements ErrorController {

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		int status = statusObj != null ? (int) statusObj : 500;
		HttpStatus httpStatus = HttpStatus.resolve(status);

		String error = httpStatus != null ? httpStatus.getReasonPhrase() : "Unknown Error";
		Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

		model.addAttribute("status", status);
		model.addAttribute("error", error);
		model.addAttribute("message", message != null && !message.toString().isEmpty()
				? message : "요청을 처리할 수 없습니다.");
		model.addAttribute("traceId", TraceIdHolder.getTraceId());
		model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

		return "error/error";
	}
}
