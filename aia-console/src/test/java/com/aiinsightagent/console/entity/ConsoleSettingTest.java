package com.aiinsightagent.console.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleSetting 테스트")
class ConsoleSettingTest {

	@Test
	@DisplayName("생성자 - 정상 생성")
	void constructor_CreatesInstance() {
		ConsoleSetting setting = new ConsoleSetting("theme", "dark", "UI 테마 설정");

		assertThat(setting.getSettingKey()).isEqualTo("theme");
		assertThat(setting.getSettingValue()).isEqualTo("dark");
		assertThat(setting.getDescription()).isEqualTo("UI 테마 설정");
		assertThat(setting.getRegDate()).isNotNull();
		assertThat(setting.getModDate()).isNull();
	}

	@Test
	@DisplayName("updateValue - 값 변경 시 modDate 갱신")
	void updateValue_UpdatesValueAndModDate() {
		ConsoleSetting setting = new ConsoleSetting("theme", "dark", "UI 테마 설정");

		setting.updateValue("light");

		assertThat(setting.getSettingValue()).isEqualTo("light");
		assertThat(setting.getModDate()).isNotNull();
	}

	@Test
	@DisplayName("updateDescription - 설명 변경 시 modDate 갱신")
	void updateDescription_UpdatesDescriptionAndModDate() {
		ConsoleSetting setting = new ConsoleSetting("theme", "dark", "UI 테마 설정");

		setting.updateDescription("새로운 설명");

		assertThat(setting.getDescription()).isEqualTo("새로운 설명");
		assertThat(setting.getModDate()).isNotNull();
	}
}
