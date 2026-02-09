package com.aiinsightagent.console.service;

import com.aiinsightagent.console.entity.ConsoleAnalysisResultView;
import com.aiinsightagent.console.entity.ConsolePreparedContextView;
import com.aiinsightagent.console.exception.ConsoleError;
import com.aiinsightagent.console.exception.ConsoleException;
import com.aiinsightagent.console.repository.ConsoleAnalysisResultRepository;
import com.aiinsightagent.console.repository.ConsolePreparedContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisMonitorService {

	private final ConsoleAnalysisResultRepository analysisResultRepository;
	private final ConsolePreparedContextRepository preparedContextRepository;

	public Page<ConsoleAnalysisResultView> getResults(String status, String type,
			String requestId, String version,
			LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		String normalizedStatus = (status != null && !status.isBlank()) ? status : null;
		String normalizedType = (type != null && !type.isBlank()) ? type : null;
		String normalizedRequestId = (requestId != null && !requestId.isBlank()) ? requestId : null;
		String normalizedVersion = (version != null && !version.isBlank()) ? version : null;
		return analysisResultRepository.search(
				normalizedStatus, normalizedType, normalizedRequestId, normalizedVersion,
				startDate, endDate, pageRequest);
	}

	public ConsoleAnalysisResultView getResultById(Long resultId) {
		return analysisResultRepository.findById(resultId)
				.orElseThrow(() -> new ConsoleException(ConsoleError.ANALYSIS_NOT_FOUND));
	}

	public List<ConsolePreparedContextView> getPreparedContextsByActorId(Long actorId) {
		return preparedContextRepository.findByActorIdAndActiveTrue(actorId);
	}
}
