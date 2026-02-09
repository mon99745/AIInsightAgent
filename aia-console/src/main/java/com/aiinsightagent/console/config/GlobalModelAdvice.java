package com.aiinsightagent.console.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

	@ModelAttribute("currentPath")
	public String currentPath(HttpServletRequest request) {
		return request.getRequestURI();
	}
}
