package com.aiinsightagent.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청마다 TraceId를 생성하여 MDC에 설정하는 필터.
 * 클라이언트가 X-Trace-Id 헤더를 전달하면 해당 값을 사용하고,
 * 없으면 새로운 UUID를 생성한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

	public static final String TRACE_ID_HEADER = "X-Trace-Id";
	public static final String TRACE_ID_MDC_KEY = "traceId";

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		try {
			String traceId = extractOrGenerateTraceId(request);
			MDC.put(TRACE_ID_MDC_KEY, traceId);
			response.setHeader(TRACE_ID_HEADER, traceId);
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(TRACE_ID_MDC_KEY);
		}
	}

	private String extractOrGenerateTraceId(HttpServletRequest request) {
		String traceId = request.getHeader(TRACE_ID_HEADER);
		if (traceId == null || traceId.isBlank()) {
			traceId = generateTraceId();
		}
		return traceId;
	}

	private String generateTraceId() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}
}
