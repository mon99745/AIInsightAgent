package com.aiinsightagent.console.service;

import com.aiinsightagent.console.dto.DashboardSummary;
import com.aiinsightagent.console.entity.ConsoleAnalysisResultView;
import com.aiinsightagent.console.repository.ConsoleAnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

	private final ConsoleAnalysisResultRepository analysisResultRepository;
	private final ActuatorProxyService actuatorProxyService;

	public DashboardSummary getSummary() {
		long totalCount = analysisResultRepository.count();
		long successCount = analysisResultRepository.countByStatus("SUCCESS");
		long failedCount = analysisResultRepository.countByStatus("FAILED");

		Map<String, Object> health = actuatorProxyService.getHealth();
		String healthStatus = String.valueOf(health.getOrDefault("status", "UNKNOWN"));

		return DashboardSummary.builder()
				.totalAnalysis(totalCount)
				.successCount(successCount)
				.failedCount(failedCount)
				.appHealthStatus(healthStatus)
				.build();
	}

	public Page<ConsoleAnalysisResultView> getRecentResults(int page, int size) {
		return analysisResultRepository.findAllByOrderByRegDateDesc(PageRequest.of(page, size));
	}
}
