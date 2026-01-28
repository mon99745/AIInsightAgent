package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.util.InsightRequestValidator;
import com.aiinsightagent.core.context.GeminiContext;
import com.aiinsightagent.core.facade.InsightFacade;
import com.aiinsightagent.core.model.InsightHistoryResponse;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {
	private final InsightFacade insightFacade;
	private final ActorService actorService;
	private final AnalysisRawDataService rawDataService;
	private final AnalysisResultService resultService;
	private final PreparedContextService contextService;

	public InsightResponse requestInsight(String purpose, String prompt) {
		log.info("answer called with purpose: {}, prompt: {}", purpose, prompt);

		return insightFacade.answer(purpose, prompt);
	}

	@Transactional
	public InsightResponse requestInsight(InsightRequest data) {
		log.info("analysis called with purpose: {}, userPrompts: {}"
				, data.getPurpose(), data.getUserPrompt());

		try {
			// 1. 요청 데이터 검증
			InsightRequestValidator.validate(data);

			// 2. 접근 주체 조회 및 저장
			Actor actor = actorService.getOrCreate(data.getUserId());

			// 3. 원본 데이터 저장
			AnalysisRawData rawData = rawDataService.save(actor, data.getPurpose(), data.getUserPrompt());

			// 4. 전처리 데이터 조회
			String contextText = contextService.findByActorKey(actor)
					.map(PreparedContext::asPromptText)
					.orElse(null);

			// 5. 분석 요청
			InsightResponse response = insightFacade.analysis(data, contextText);

			// 6. 결과 저장
			String analysisVersion = GeminiContext.getAnalysisVersion();
			resultService.save(actor, rawData, response, analysisVersion);

			return response;
		} finally {
			// ThreadLocal 메모리 누수 방지
			GeminiContext.clear();
		}
	}

	public InsightHistoryResponse getHistory(String userId) {
		Actor actor = actorService.get(userId);

		return rawDataService.getUserPromtListByActor(actor);
	}
}
