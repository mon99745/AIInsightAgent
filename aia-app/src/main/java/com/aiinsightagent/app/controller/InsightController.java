package com.aiinsightagent.app.controller;


import com.aiinsightagent.core.message.InsightRequest;
import com.aiinsightagent.core.message.InsightResponse;
import com.aiinsightagent.core.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = InsightController.TAG, description = "인공지능을 통한 데이터 분석 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping(InsightController.PATH)
public class InsightController {
	public static final String TAG = "AI Insight API";
	public static final String PATH = "/api/v1/insight";
	private final InsightService insightService;

	@Operation(summary = "Single AI analysis request")
	@GetMapping("single")
	public InsightResponse getSingleAnalysis(@RequestParam String purpose, @RequestParam String prompt) {
		return insightService.getSingleAnswer(purpose, prompt);
	}

	@Operation(summary = "Multiple AI analysis request")
	@PostMapping("multiple")
	public InsightResponse getMultipleAnalysis(InsightRequest request) {
		return insightService.getMultipleAnswer(request);
	}
}
