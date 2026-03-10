package com.aiinsightagent.console.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginController 테스트")
class LoginControllerTest {

	private final LoginController loginController = new LoginController();

	@Test
	@DisplayName("loginPage - 파라미터 없으면 로그인 뷰 반환")
	void loginPage_ReturnsLoginView() {
		Model model = new ConcurrentModel();

		String viewName = loginController.loginPage(null, null, model);

		assertThat(viewName).isEqualTo("auth/login");
		assertThat(model.getAttribute("errorMessage")).isNull();
		assertThat(model.getAttribute("logoutMessage")).isNull();
	}

	@Test
	@DisplayName("loginPage - error 파라미터 있으면 에러 메시지 모델에 추가")
	void loginPage_WithErrorParam_AddsErrorMessage() {
		Model model = new ConcurrentModel();

		String viewName = loginController.loginPage("true", null, model);

		assertThat(viewName).isEqualTo("auth/login");
		assertThat(model.getAttribute("errorMessage")).isNotNull();
		assertThat(model.getAttribute("errorMessage").toString()).contains("아이디 또는 비밀번호");
	}

	@Test
	@DisplayName("loginPage - logout 파라미터 있으면 로그아웃 메시지 모델에 추가")
	void loginPage_WithLogoutParam_AddsLogoutMessage() {
		Model model = new ConcurrentModel();

		String viewName = loginController.loginPage(null, "true", model);

		assertThat(viewName).isEqualTo("auth/login");
		assertThat(model.getAttribute("logoutMessage")).isNotNull();
		assertThat(model.getAttribute("logoutMessage").toString()).contains("로그아웃");
	}

	@Test
	@DisplayName("loginPage - error와 logout 동시 파라미터 처리")
	void loginPage_WithBothParams_AddsBothMessages() {
		Model model = new ConcurrentModel();

		loginController.loginPage("true", "true", model);

		assertThat(model.getAttribute("errorMessage")).isNotNull();
		assertThat(model.getAttribute("logoutMessage")).isNotNull();
	}
}
