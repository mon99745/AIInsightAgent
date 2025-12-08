package com.aiinsightagent.app.controller;

import com.aiinsightagent.core.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(InsightController.PATH)
public class InsightController {
	public static final String TAG = "AI Insight API";
	public static final String PATH = "/api/v1";

	protected final InsightService insightService;

	@PostMapping("/insight")
	public String generateInsight(@RequestBody String data) {
		return insightService.generateInsight(data);
	}
}
