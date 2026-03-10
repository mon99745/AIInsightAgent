package com.aiinsightagent.console.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

	@GetMapping("/login")
	public String loginPage(
			@RequestParam(required = false) String error,
			@RequestParam(required = false) String logout,
			Model model) {

		if (error != null) {
			model.addAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		if (logout != null) {
			model.addAttribute("logoutMessage", "로그아웃되었습니다.");
		}
		return "auth/login";
	}
}
