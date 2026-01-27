package com.aiinsightagent.common.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdHolderTest {

	@BeforeEach
	void setUp() {
		MDC.clear();
	}

	@AfterEach
	void tearDown() {
		MDC.clear();
	}

	@Test
	@DisplayName("MDC에 TraceId가 있으면 해당 값을 반환한다")
	void getTraceId_withMdcValue_returnsValue() {
		// given
		String expectedTraceId = "abc123def456";
		MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, expectedTraceId);

		// when
		String traceId = TraceIdHolder.getTraceId();

		// then
		assertThat(traceId).isEqualTo(expectedTraceId);
	}

	@Test
	@DisplayName("MDC에 TraceId가 없으면 'unknown'을 반환한다")
	void getTraceId_withoutMdcValue_returnsUnknown() {
		// when
		String traceId = TraceIdHolder.getTraceId();

		// then
		assertThat(traceId).isEqualTo("unknown");
	}

	@Test
	@DisplayName("MDC에서 TraceId가 제거되면 'unknown'을 반환한다")
	void getTraceId_afterMdcClear_returnsUnknown() {
		// given
		MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, "some-trace-id");
		MDC.remove(TraceIdFilter.TRACE_ID_MDC_KEY);

		// when
		String traceId = TraceIdHolder.getTraceId();

		// then
		assertThat(traceId).isEqualTo("unknown");
	}
}
