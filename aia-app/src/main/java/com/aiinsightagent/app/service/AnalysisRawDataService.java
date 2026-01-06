package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import com.aiinsightagent.app.enums.InputType;
import com.aiinsightagent.app.repository.AnalysisRawDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisRawDataService {
	private final AnalysisRawDataRepository rawDataRepository;

	public AnalysisRawData save(Actor actor, String rawPayload) {
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.JSON, rawPayload);

		return rawDataRepository.save(rawData);
	}
}