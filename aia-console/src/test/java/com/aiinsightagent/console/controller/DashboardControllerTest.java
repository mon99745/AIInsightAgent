package com.aiinsightagent.console.controller;

import com.aiinsightagent.console.dto.DashboardSummary;
import com.aiinsightagent.console.entity.ConsoleAnalysisResultView;
import com.aiinsightagent.console.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController 테스트")
class DashboardControllerTest {

	@Mock
	private DashboardService dashboardService;

	@InjectMocks
	private DashboardController dashboardController;

	@Test
	@DisplayName("index - 대시보드 페이지 정상 반환")
	void index_ReturnsDashboardView() {
		DashboardSummary summary = DashboardSummary.builder()
				.totalAnalysis(50)
				.successCount(40)
				.failedCount(10)
				.appHealthStatus("UP")
				.build();
		Page<ConsoleAnalysisResultView> recentResults = new PageImpl<>(List.of());

		given(dashboardService.getSummary()).willReturn(summary);
		given(dashboardService.getRecentResults(0, 10)).willReturn(recentResults);

		Model model = new ConcurrentModel();
		String viewName = dashboardController.index(0, 10, model);

		assertThat(viewName).isEqualTo("dashboard/index");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Dashboard");
		assertThat(model.getAttribute("summary")).isEqualTo(summary);
		assertThat(model.getAttribute("recentResults")).isEqualTo(recentResults);
	}

	@Test
	@DisplayName("index - 커스텀 page/size 파라미터 전달")
	void index_WithCustomPageAndSize() {
		DashboardSummary summary = DashboardSummary.builder().build();
		Page<ConsoleAnalysisResultView> recentResults = new PageImpl<>(List.of());

		given(dashboardService.getSummary()).willReturn(summary);
		given(dashboardService.getRecentResults(2, 5)).willReturn(recentResults);

		Model model = new ConcurrentModel();
		String viewName = dashboardController.index(2, 5, model);

		assertThat(viewName).isEqualTo("dashboard/index");
	}
}
