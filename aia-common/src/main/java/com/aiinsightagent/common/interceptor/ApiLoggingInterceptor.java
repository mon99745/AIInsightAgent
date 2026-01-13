package com.aiinsightagent.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

	private static final String START_TIME_ATTRIBUTE = "apiStartTime";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
		log.info("[API Request] {} {} started", request.getMethod(), request.getRequestURI());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
								Object handler, Exception ex) {
		Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
		if (startTime != null) {
			long duration = System.currentTimeMillis() - startTime;
			int status = response.getStatus();

			if (ex != null) {
				log.warn("[API Response] {} {} completed with status {} in {}ms (exception: {})",
						request.getMethod(), request.getRequestURI(), status, duration, ex.getMessage());
			} else {
				log.info("[API Response] {} {} completed with status {} in {}ms",
						request.getMethod(), request.getRequestURI(), status, duration);
			}
		}
	}
}
