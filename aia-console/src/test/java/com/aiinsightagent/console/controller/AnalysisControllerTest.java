package com.aiinsightagent.console.controller;

import com.aiinsightagent.console.entity.ConsoleAnalysisResultView;
import com.aiinsightagent.console.service.AnalysisMonitorService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisController 테스트")
class AnalysisControllerTest {

	@Mock
	private AnalysisMonitorService analysisMonitorService;

	@InjectMocks
	private AnalysisController analysisController;

	@Test
	@DisplayName("list - 분석 결과 목록 페이지 반환")
	void list_ReturnsListView() {
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list(null, null, null, null, null, null, 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Analysis Monitor");
		assertThat(model.getAttribute("results")).isEqualTo(results);
		assertThat(model.getAttribute("currentStatus")).isNull();
		assertThat(model.getAttribute("currentType")).isNull();
		assertThat(model.getAttribute("currentRequestId")).isNull();
		assertThat(model.getAttribute("currentVersion")).isNull();
		assertThat(model.getAttribute("currentStartDate")).isNull();
		assertThat(model.getAttribute("currentEndDate")).isNull();
	}

	@Test
	@DisplayName("list - status 필터 적용")
	void list_WithStatusFilter() {
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(eq("SUCCESS"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list("SUCCESS", null, null, null, null, null, 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
		assertThat(model.getAttribute("currentStatus")).isEqualTo("SUCCESS");
	}

	@Test
	@DisplayName("list - type 필터 적용")
	void list_WithTypeFilter() {
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(isNull(), eq("STYLE"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list(null, "STYLE", null, null, null, null, 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
		assertThat(model.getAttribute("currentType")).isEqualTo("STYLE");
	}

	@Test
	@DisplayName("list - requestId 필터 적용")
	void list_WithRequestIdFilter() {
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(isNull(), isNull(), eq("abc-123"), isNull(), isNull(), isNull(), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list(null, null, "abc-123", null, null, null, 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
		assertThat(model.getAttribute("currentRequestId")).isEqualTo("abc-123");
	}

	@Test
	@DisplayName("list - version 필터 적용")
	void list_WithVersionFilter() {
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(isNull(), isNull(), isNull(), eq("v1.0"), isNull(), isNull(), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list(null, null, null, "v1.0", null, null, 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
		assertThat(model.getAttribute("currentVersion")).isEqualTo("v1.0");
	}

	@Test
	@DisplayName("list - 날짜 범위 검색")
	void list_WithDateRange() {
		LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 1, 0, 0, 0);
		LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 15, 23, 59, 59);
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(isNull(), isNull(), isNull(), isNull(), eq(startDateTime), eq(endDateTime), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list(null, null, null, null, "2025-03-01", "2025-03-15", 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
		assertThat(model.getAttribute("currentStartDate")).isEqualTo("2025-03-01");
		assertThat(model.getAttribute("currentEndDate")).isEqualTo("2025-03-15");
	}

	@Test
	@DisplayName("list - 잘못된 날짜 형식은 날짜 필터 무시")
	void list_WithInvalidDate_IgnoresDateFilter() {
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list(null, null, null, null, "invalid", "invalid", 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
	}

	@Test
	@DisplayName("list - 모든 필터 동시 적용")
	void list_WithAllFilters() {
		LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 1, 0, 0, 0);
		LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 31, 23, 59, 59);
		Page<ConsoleAnalysisResultView> results = new PageImpl<>(List.of());
		given(analysisMonitorService.getResults(eq("SUCCESS"), eq("PATTERN"), eq("abc"), eq("v1.0"), eq(startDateTime), eq(endDateTime), eq(0), eq(20)))
				.willReturn(results);

		Model model = new ConcurrentModel();
		String viewName = analysisController.list("SUCCESS", "PATTERN", "abc", "v1.0", "2025-03-01", "2025-03-31", 0, 20, model);

		assertThat(viewName).isEqualTo("analysis/list");
		assertThat(model.getAttribute("currentStatus")).isEqualTo("SUCCESS");
		assertThat(model.getAttribute("currentType")).isEqualTo("PATTERN");
		assertThat(model.getAttribute("currentRequestId")).isEqualTo("abc");
		assertThat(model.getAttribute("currentVersion")).isEqualTo("v1.0");
		assertThat(model.getAttribute("currentStartDate")).isEqualTo("2025-03-01");
		assertThat(model.getAttribute("currentEndDate")).isEqualTo("2025-03-31");
	}

	@Test
	@DisplayName("detail - 분석 결과 상세 페이지 반환 (PreparedContext 포함)")
	void detail_ReturnsDetailView() {
		ConsoleAnalysisResultView mockResult = mock(ConsoleAnalysisResultView.class);
		given(mockResult.getActorId()).willReturn(10L);
		given(analysisMonitorService.getResultById(1L)).willReturn(mockResult);
		given(analysisMonitorService.getPreparedContextsByActorId(10L)).willReturn(List.of());

		Model model = new ConcurrentModel();
		String viewName = analysisController.detail(1L, model);

		assertThat(viewName).isEqualTo("analysis/detail");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Analysis Detail");
		assertThat(model.getAttribute("result")).isEqualTo(mockResult);
		assertThat(model.getAttribute("preparedContexts")).isEqualTo(List.of());
	}
}
