package com.aiinsightagent.app.service;

import com.aiinsightagent.app.util.InsightRequestValidator;
import com.aiinsightagent.core.facade.InsightFacade;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.InsightResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {
	private final InsightFacade insightFacade;

	public InsightResponse requestInsight(String purpose, String prompt) {
		log.info("answer called with purpose: {}, prompt: {}", purpose, prompt);

		return insightFacade.answer(purpose, prompt);
	}

	public InsightResponse requestInsight(InsightRequest data) {
		log.info("analysis called with purpose: {}, userPrompts: {}", data.getPurpose(), data.getUserPrompt());

		InsightRequestValidator.validate(data);

		return insightFacade.analysis(data);
	}
}
