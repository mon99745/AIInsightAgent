package com.aiinsightagent.console.controller;

import com.aiinsightagent.console.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/")
	public String root() {
		return "redirect:/dashboard";
	}

	@GetMapping("/dashboard")
	public String index(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			Model model) {

		model.addAttribute("pageTitle", "Dashboard");
		model.addAttribute("summary", dashboardService.getSummary());
		model.addAttribute("recentResults", dashboardService.getRecentResults(page, size));

		return "dashboard/index";
	}
}
