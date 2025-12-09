package com.aiinsightagent.app.controller;


import com.aiinsightagent.common.message.InsightResponse;
import com.aiinsightagent.core.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(InsightController.PATH)
public class InsightController {
	public static final String PATH = "/api/v1";
	private final InsightService insightService;

	@Operation(summary = "AI 답변 생성 요청")
	@GetMapping("generate/answer")
	public InsightResponse getAnswer(@RequestParam String prompt) {
		return insightService.getAnswer(prompt);
	}
}
