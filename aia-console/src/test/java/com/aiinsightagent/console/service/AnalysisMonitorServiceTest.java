package com.aiinsightagent.console.service;

import com.aiinsightagent.console.entity.ConsoleAnalysisResultView;
import com.aiinsightagent.console.entity.ConsolePreparedContextView;
import com.aiinsightagent.console.exception.ConsoleException;
import com.aiinsightagent.console.repository.ConsoleAnalysisResultRepository;
import com.aiinsightagent.console.repository.ConsolePreparedContextRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisMonitorService 테스트")
class AnalysisMonitorServiceTest {

	@Mock
	private ConsoleAnalysisResultRepository analysisResultRepository;

	@Mock
	private ConsolePreparedContextRepository preparedContextRepository;

	@InjectMocks
	private AnalysisMonitorService analysisMonitorService;

	@Test
	@DisplayName("getResults - status 필터로 조회")
	void getResults_WithStatus_FiltersByStatus() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(eq("SUCCESS"), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults("SUCCESS", null, null, null, null, null, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(eq("SUCCESS"), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - type 필터로 조회")
	void getResults_WithType_FiltersByType() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(isNull(), eq("STYLE"), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults(null, "STYLE", null, null, null, null, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(isNull(), eq("STYLE"), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - 필터 없이 전체 조회")
	void getResults_WithoutFilters_ReturnsAll() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults(null, null, null, null, null, null, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - 빈 문자열 status는 필터 적용하지 않음")
	void getResults_WithBlankStatus_ReturnsAll() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults("  ", null, null, null, null, null, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - status와 type 동시 필터링")
	void getResults_WithBothStatusAndType_FiltersBoth() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(eq("FAILED"), eq("STYLE"), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults("FAILED", "STYLE", null, null, null, null, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(eq("FAILED"), eq("STYLE"), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - requestId 필터로 조회")
	void getResults_WithRequestId_FiltersByRequestId() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(isNull(), isNull(), eq("abc-123"), isNull(), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults(null, null, "abc-123", null, null, null, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(isNull(), isNull(), eq("abc-123"), isNull(), isNull(), isNull(), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - version 필터로 조회")
	void getResults_WithVersion_FiltersByVersion() {
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(isNull(), isNull(), isNull(), eq("v1.0"), isNull(), isNull(), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults(null, null, null, "v1.0", null, null, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(isNull(), isNull(), isNull(), eq("v1.0"), isNull(), isNull(), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - 날짜 범위 검색")
	void getResults_WithDateRange_FiltersByDate() {
		LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59, 59);
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(isNull(), isNull(), isNull(), isNull(), eq(startDate), eq(endDate), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults(null, null, null, null, startDate, endDate, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(isNull(), isNull(), isNull(), isNull(), eq(startDate), eq(endDate), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResults - 모든 필터 동시 적용")
	void getResults_WithAllFilters() {
		LocalDateTime startDate = LocalDateTime.of(2025, 3, 1, 0, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2025, 3, 31, 23, 59, 59);
		Page<ConsoleAnalysisResultView> expectedPage = new PageImpl<>(List.of());
		given(analysisResultRepository.search(eq("SUCCESS"), eq("PATTERN"), eq("abc"), eq("v1.0"), eq(startDate), eq(endDate), any(PageRequest.class)))
				.willReturn(expectedPage);

		Page<ConsoleAnalysisResultView> result = analysisMonitorService.getResults("SUCCESS", "PATTERN", "abc", "v1.0", startDate, endDate, 0, 20);

		assertThat(result).isEqualTo(expectedPage);
		verify(analysisResultRepository).search(eq("SUCCESS"), eq("PATTERN"), eq("abc"), eq("v1.0"), eq(startDate), eq(endDate), any(PageRequest.class));
	}

	@Test
	@DisplayName("getResultById - 존재하는 ID로 조회")
	void getResultById_WithExistingId_ReturnsResult() {
		ConsoleAnalysisResultView mockResult = mock(ConsoleAnalysisResultView.class);
		given(analysisResultRepository.findById(1L)).willReturn(Optional.of(mockResult));

		ConsoleAnalysisResultView result = analysisMonitorService.getResultById(1L);

		assertThat(result).isEqualTo(mockResult);
	}

	@Test
	@DisplayName("getResultById - 존재하지 않는 ID로 조회 시 예외 발생")
	void getResultById_WithNonExistingId_ThrowsException() {
		given(analysisResultRepository.findById(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> analysisMonitorService.getResultById(999L))
				.isInstanceOf(ConsoleException.class);
	}

	@Test
	@DisplayName("getPreparedContextsByActorId - 활성화된 PreparedContext 조회")
	void getPreparedContextsByActorId_ReturnsActiveContexts() {
		ConsolePreparedContextView mockCtx = mock(ConsolePreparedContextView.class);
		given(preparedContextRepository.findByActorIdAndActiveTrue(1L)).willReturn(List.of(mockCtx));

		List<ConsolePreparedContextView> result = analysisMonitorService.getPreparedContextsByActorId(1L);

		assertThat(result).hasSize(1);
		verify(preparedContextRepository).findByActorIdAndActiveTrue(1L);
	}
}
