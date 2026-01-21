package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import com.aiinsightagent.app.entity.AnalysisResult;
import com.aiinsightagent.app.enums.AnalysisStatus;
import com.aiinsightagent.app.enums.AnalysisType;
import com.aiinsightagent.app.repository.AnalysisResultRepository;
import com.aiinsightagent.app.util.InsightResultSerializer;
import com.aiinsightagent.core.model.InsightResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisResultService {
	private final AnalysisResultRepository resultRepository;
	private final InsightResultSerializer serializer;

	public AnalysisResult save(Actor actor, AnalysisRawData rawData, InsightResponse result, String analysisVersion) {
		String resultPayload = serializer.serialize(result.getInsight());
		AnalysisResult analysisResult = new AnalysisResult(
				actor,
				rawData,
				AnalysisType.STYLE,
				AnalysisStatus.SUCCESS,
				resultPayload,
				analysisVersion
		);

		return resultRepository.save(analysisResult);
	}
}