package com.aiinsightagent.common.filter;

import org.slf4j.MDC;

/**
 * MDC에서 TraceId를 가져오는 유틸리티 클래스
 */
public final class TraceIdHolder {

	private TraceIdHolder() {
	}

	/**
	 * 현재 요청의 TraceId를 반환한다.
	 * MDC에 값이 없으면 "unknown"을 반환한다.
	 */
	public static String getTraceId() {
		String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
		return traceId != null ? traceId : "unknown";
	}
}
