package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import com.aiinsightagent.app.enums.InputType;
import com.aiinsightagent.app.exception.InsightAppError;
import com.aiinsightagent.app.repository.AnalysisRawDataRepository;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightHistoryResponse;
import com.aiinsightagent.core.model.InsightRecord;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisRawDataService {
	private final ObjectMapper objectMapper;
	private final AnalysisRawDataRepository rawDataRepository;

	public AnalysisRawData save(Actor actor, String purpose, List<UserPrompt> userPrompt) {
		String jsonPayload = convertToJson(userPrompt);

		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.JSON, purpose, jsonPayload);

		return rawDataRepository.save(rawData);
	}

	public InsightHistoryResponse getUserPromtListByActor(Actor actor) {
		// 1. AnalysisRawData 리스트 조회
		List<AnalysisRawData> analysisRawDatas = rawDataRepository.findAllByActor(actor);

		// 3. InsightRecord 리스트로 변환
		List<InsightRecord> insightRecords = analysisRawDatas.stream()
				.flatMap(data -> parseUserPrompts(data.getRawPayload()).stream()
						.map(userPrompt -> InsightRecord.builder()
								.inputId(data.getInputId())
								.regDate(data.getRegDate())
								.userPrompt(userPrompt)
								.build()))
				.collect(Collectors.toList());

		return InsightHistoryResponse.builder()
				.resultCode(HttpStatus.OK.value())
				.resultMsg(HttpStatus.OK.getReasonPhrase())
				.insightRecords(insightRecords)
				.build();
	}

	private List<UserPrompt> parseUserPrompts(String rawPayload) {
		try {
			// JSON 데이터 로깅
			log.info("Parsing rawPayload: {}", rawPayload);

			return objectMapper.readValue(
					rawPayload,
					new TypeReference<List<UserPrompt>>() {}
			);
		} catch (JsonProcessingException e) {
			log.error("JSON parsing failed. Raw data: {}", rawPayload, e);
			throw new InsightException(InsightAppError.FAIL_JSON_PARSING_RAW_DATA);
		}
	}

	private String convertToJson(List<UserPrompt> userPrompts) {
		try {
			return objectMapper.writeValueAsString(userPrompts);
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize userPrompts to JSON", e);
			throw new InsightException(InsightAppError.FAIL_JSON_SERIALIZATION);
		}
	}
}