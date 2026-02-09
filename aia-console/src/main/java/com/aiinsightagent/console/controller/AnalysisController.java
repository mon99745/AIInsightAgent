package com.aiinsightagent.console.controller;

import com.aiinsightagent.console.service.AnalysisMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

	private final AnalysisMonitorService analysisMonitorService;

	@GetMapping
	public String list(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String requestId,
			@RequestParam(required = false) String version,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			Model model) {

		LocalDateTime startDateTime = parseStartOfDay(startDate);
		LocalDateTime endDateTime = parseEndOfDay(endDate);

		model.addAttribute("pageTitle", "Analysis Monitor");
		model.addAttribute("results",
				analysisMonitorService.getResults(status, type, requestId, version,
						startDateTime, endDateTime, page, size));
		model.addAttribute("currentStatus", status);
		model.addAttribute("currentType", type);
		model.addAttribute("currentRequestId", requestId);
		model.addAttribute("currentVersion", version);
		model.addAttribute("currentStartDate", startDate);
		model.addAttribute("currentEndDate", endDate);

		return "analysis/list";
	}

	@GetMapping("/{id}")
	public String detail(@PathVariable Long id, Model model) {
		var result = analysisMonitorService.getResultById(id);

		model.addAttribute("pageTitle", "Analysis Detail");
		model.addAttribute("result", result);
		model.addAttribute("preparedContexts",
				analysisMonitorService.getPreparedContextsByActorId(result.getActorId()));

		return "analysis/detail";
	}

	private LocalDateTime parseStartOfDay(String dateStr) {
		if (dateStr == null || dateStr.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(dateStr).atStartOfDay();
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private LocalDateTime parseEndOfDay(String dateStr) {
		if (dateStr == null || dateStr.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(dateStr).atTime(23, 59, 59);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
