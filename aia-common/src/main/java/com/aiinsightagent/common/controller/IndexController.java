package com.aiinsightagent.common.controller;

@Controller
public class IndexController {
	@GetMapping("/")
	public String index() {
		return "redirect:/api";
	}
}
