package com.aiinsightagent.console.service;

import com.aiinsightagent.console.dto.DashboardSummary;
import com.aiinsightagent.console.entity.ConsoleAnalysisResultView;
import com.aiinsightagent.console.repository.ConsoleAnalysisResultRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService 테스트")
class DashboardServiceTest {

	@Mock
	private ConsoleAnalysisResultRepository analysisResultRepository;

	@Mock
	private ActuatorProxyService actuatorProxyService;

	@InjectMocks
	private DashboardService dashboardService;

	@Test
	@DisplayName("getSummary - 정상적으로 요약 데이터 반환")
	void getSummary_ReturnsSummary() {
		given(analysisResultRepository.count()).willReturn(100L);
		given(analysisResultRepository.countByStatus("SUCCESS")).willReturn(80L);
		given(analysisResultRepository.countByStatus("FAILED")).willReturn(20L);
		given(actuatorProxyService.getHealth()).willReturn(Map.of("status", "UP"));

		DashboardSummary summary = dashboardService.getSummary();

		assertThat(summary.getTotalAnalysis()).isEqualTo(100);
		assertThat(summary.getSuccessCount()).isEqualTo(80);
		assertThat(summary.getFailedCount()).isEqualTo(20);
		assertThat(summary.getAppHealthStatus()).isEqualTo("UP");
	}

	@Test
	@DisplayName("getSummary - health 응답에 status가 없으면 UNKNOWN 반환")
	void getSummary_WhenHealthStatusMissing_ReturnsUnknown() {
		given(analysisResultRepository.count()).willReturn(0L);
		given(analysisResultRepository.countByStatus("SUCCESS")).willReturn(0L);
		given(analysisResultRepository.countByStatus("FAILED")).willReturn(0L);
		given(actuatorProxyService.getHealth()).willReturn(Collections.emptyMap());

		DashboardSummary summary = dashboardService.getSummary();

		assertThat(summary.getAppHealthStatus()).isEqualTo("UNKNOWN");
	}

	@Test
	@DisplayName("getSummary - 분석 결과가 없는 경우")
	void getSummary_WhenNoResults_ReturnsZeroCounts() {
		given(analysisResultRepository.count()).willReturn(0L);
		given(analysisResultRepository.countByStatus("SUCCESS")).willReturn(0L);
		given(analysisResultRepository.countByStatus("FAILED")).willReturn(0L);
		given(actuatorProxyService.getHealth()).willReturn(Map.of("status", "DOWN"));

		DashboardSummary summary = dashboardService.getSummary();

		assertThat(summary.getTotalAnalysis()).isZero();
		assertThat(summary.getSuccessCount()).isZero();
		assertThat(summary.getFailedCount()).isZero();
		assertThat(summary.getAppHealthStatus()).isEqualTo("DOWN");
	}

	@Test
	@DisplayName("getRecentResults - 페이지네이션된 결과 반환")
	void getRecentResults_ReturnsPagedResults() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.findAllByOrderByRegDateDesc(any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = dashboardService.getRecentResults(0, 10);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).findAllByOrderByRegDateDesc(PageRequest.of(0, 10));
	}
}
