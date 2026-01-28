package com.aiinsightagent.common.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IndexControllerTest {

	private IndexController controller;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		controller = new IndexController();
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Nested
	@DisplayName("index 테스트")
	class IndexTest {

		@Test
		@DisplayName("루트 경로 요청 시 /api로 리다이렉트 문자열을 반환한다")
		void index_returnsRedirectString() {
			// when
			String result = controller.index();

			// then
			assertThat(result).isEqualTo("redirect:/api");
		}

		@Test
		@DisplayName("루트 경로 GET 요청 시 /api로 리다이렉트한다")
		void index_redirectsToApi() throws Exception {
			// when & then
			mockMvc.perform(get("/"))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/api"));
		}
	}
}
