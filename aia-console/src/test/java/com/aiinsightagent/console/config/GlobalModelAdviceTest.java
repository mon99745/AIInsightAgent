package com.aiinsightagent.console.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalModelAdvice 테스트")
class GlobalModelAdviceTest {

	@InjectMocks
	private GlobalModelAdvice globalModelAdvice;

	@Mock
	private HttpServletRequest request;

	@Test
	@DisplayName("currentPath - 요청 URI 반환")
	void currentPath_ReturnsRequestURI() {
		given(request.getRequestURI()).willReturn("/dashboard");

		String result = globalModelAdvice.currentPath(request);

		assertThat(result).isEqualTo("/dashboard");
	}

	@Test
	@DisplayName("currentPath - 루트 경로")
	void currentPath_WithRootPath() {
		given(request.getRequestURI()).willReturn("/");

		String result = globalModelAdvice.currentPath(request);

		assertThat(result).isEqualTo("/");
	}

	@Test
	@DisplayName("currentPath - 중첩된 경로")
	void currentPath_WithNestedPath() {
		given(request.getRequestURI()).willReturn("/analysis/123");

		String result = globalModelAdvice.currentPath(request);

		assertThat(result).isEqualTo("/analysis/123");
	}

	@Test
	@DisplayName("currentPath - 쿼리 파라미터는 포함되지 않음")
	void currentPath_WithoutQueryParameters() {
		given(request.getRequestURI()).willReturn("/analysis");

		String result = globalModelAdvice.currentPath(request);

		assertThat(result).isEqualTo("/analysis");
		assertThat(result).doesNotContain("?");
	}

	@Test
	@DisplayName("currentPath - 시스템 경로")
	void currentPath_WithSystemPath() {
		given(request.getRequestURI()).willReturn("/system/status");

		String result = globalModelAdvice.currentPath(request);

		assertThat(result).isEqualTo("/system/status");
	}
}