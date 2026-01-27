package com.aiinsightagent.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

	private TraceIdFilter traceIdFilter;

	@Mock
	private FilterChain filterChain;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		traceIdFilter = new TraceIdFilter();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		MDC.clear();
	}

	@Nested
	@DisplayName("TraceId 생성 테스트")
	class TraceIdGenerationTest {

		@Test
		@DisplayName("X-Trace-Id 헤더가 없으면 새로운 TraceId를 생성한다")
		void doFilter_noHeader_generatesNewTraceId() throws ServletException, IOException {
			// when
			traceIdFilter.doFilterInternal(request, response, filterChain);

			// then
			String traceId = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
			assertThat(traceId).isNotNull();
			assertThat(traceId).hasSize(16);
			assertThat(traceId).matches("[a-f0-9]+");
		}

		@Test
		@DisplayName("X-Trace-Id 헤더가 있으면 해당 값을 사용한다")
		void doFilter_withHeader_usesProvidedTraceId() throws ServletException, IOException {
			// given
			String providedTraceId = "custom-trace-id-123";
			request.addHeader(TraceIdFilter.TRACE_ID_HEADER, providedTraceId);

			// when
			traceIdFilter.doFilterInternal(request, response, filterChain);

			// then
			String traceId = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
			assertThat(traceId).isEqualTo(providedTraceId);
		}

		@Test
		@DisplayName("X-Trace-Id 헤더가 빈 문자열이면 새로운 TraceId를 생성한다")
		void doFilter_emptyHeader_generatesNewTraceId() throws ServletException, IOException {
			// given
			request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "");

			// when
			traceIdFilter.doFilterInternal(request, response, filterChain);

			// then
			String traceId = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
			assertThat(traceId).isNotNull();
			assertThat(traceId).hasSize(16);
		}

		@Test
		@DisplayName("X-Trace-Id 헤더가 공백만 있으면 새로운 TraceId를 생성한다")
		void doFilter_blankHeader_generatesNewTraceId() throws ServletException, IOException {
			// given
			request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "   ");

			// when
			traceIdFilter.doFilterInternal(request, response, filterChain);

			// then
			String traceId = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
			assertThat(traceId).isNotNull();
			assertThat(traceId).hasSize(16);
		}
	}

	@Nested
	@DisplayName("MDC 관리 테스트")
	class MdcManagementTest {

		@Test
		@DisplayName("필터 실행 중 MDC에 TraceId가 설정된다")
		void doFilter_setsMdcDuringExecution() throws ServletException, IOException {
			// given
			final String[] capturedTraceId = new String[1];

			// when
			traceIdFilter.doFilterInternal(request, response, (req, res) -> {
				capturedTraceId[0] = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
			});

			// then
			assertThat(capturedTraceId[0]).isNotNull();
			assertThat(capturedTraceId[0]).hasSize(16);
		}

		@Test
		@DisplayName("필터 실행 후 MDC에서 TraceId가 제거된다")
		void doFilter_clearsMdcAfterExecution() throws ServletException, IOException {
			// when
			traceIdFilter.doFilterInternal(request, response, filterChain);

			// then
			assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
		}

		@Test
		@DisplayName("예외 발생 시에도 MDC가 정리된다")
		void doFilter_clearsMdcOnException() throws ServletException, IOException {
			// given
			RuntimeException expectedException = new RuntimeException("Test exception");

			// when & then
			try {
				traceIdFilter.doFilterInternal(request, response, (req, res) -> {
					throw expectedException;
				});
			} catch (RuntimeException e) {
				assertThat(e).isEqualTo(expectedException);
			}

			assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
		}
	}

	@Nested
	@DisplayName("FilterChain 호출 테스트")
	class FilterChainTest {

		@Test
		@DisplayName("FilterChain.doFilter가 호출된다")
		void doFilter_callsFilterChain() throws ServletException, IOException {
			// when
			traceIdFilter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);
		}
	}

	@Nested
	@DisplayName("응답 헤더 테스트")
	class ResponseHeaderTest {

		@Test
		@DisplayName("응답 헤더에 X-Trace-Id가 설정된다")
		void doFilter_setsResponseHeader() throws ServletException, IOException {
			// when
			traceIdFilter.doFilterInternal(request, response, filterChain);

			// then
			assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isNotNull();
		}

		@Test
		@DisplayName("요청의 TraceId와 응답의 TraceId가 동일하다")
		void doFilter_requestAndResponseTraceIdMatch() throws ServletException, IOException {
			// given
			final String[] capturedTraceId = new String[1];

			// when
			traceIdFilter.doFilterInternal(request, response, (req, res) -> {
				capturedTraceId[0] = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
			});

			// then
			assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER))
					.isEqualTo(capturedTraceId[0]);
		}
	}
}
