package com.aiinsightagent.console.controller;

import com.aiinsightagent.console.service.ActuatorProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemStatusController {

	private final ActuatorProxyService actuatorProxyService;

	@GetMapping("/status")
	public String status(Model model) {
		model.addAttribute("pageTitle", "System Status");
		model.addAttribute("health", actuatorProxyService.getHealth());
		model.addAttribute("info", actuatorProxyService.getInfo());

		return "system/status";
	}
}
