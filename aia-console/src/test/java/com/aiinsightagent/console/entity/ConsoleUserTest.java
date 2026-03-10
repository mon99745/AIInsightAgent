package com.aiinsightagent.console.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleUser 테스트")
class ConsoleUserTest {

	@Test
	@DisplayName("생성자 - 정상 생성")
	void constructor_CreatesInstance() {
		ConsoleUser user = new ConsoleUser("admin", "hashedPassword");

		assertThat(user.getUsername()).isEqualTo("admin");
		assertThat(user.getPassword()).isEqualTo("hashedPassword");
		assertThat(user.isEnabled()).isTrue();
		assertThat(user.getRegDate()).isNotNull();
	}

	@Test
	@DisplayName("changePassword - 비밀번호 변경")
	void changePassword_UpdatesPassword() {
		ConsoleUser user = new ConsoleUser("admin", "oldHash");

		user.changePassword("newHash");

		assertThat(user.getPassword()).isEqualTo("newHash");
	}
}
