package com.aiinsightagent.console.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleError 테스트")
class ConsoleErrorTest {

	@Test
	@DisplayName("CODE_PREFIX가 CON-으로 정의됨")
	void codePrefix_IsCon() {
		assertThat(ConsoleError.CODE_PREFIX).isEqualTo("CON-");
	}

	@Test
	@DisplayName("SETTING_NOT_FOUND 에러 코드와 상태 검증")
	void settingNotFound_HasCorrectCodeAndStatus() {
		ConsoleError error = ConsoleError.SETTING_NOT_FOUND;

		assertThat(error.getCode()).isEqualTo("CON-01-01");
		assertThat(error.getMessage()).isEqualTo("설정을 찾을 수 없습니다.");
		assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("SETTING_KEY_DUPLICATE 에러 코드와 상태 검증")
	void settingKeyDuplicate_HasCorrectCodeAndStatus() {
		ConsoleError error = ConsoleError.SETTING_KEY_DUPLICATE;

		assertThat(error.getCode()).isEqualTo("CON-01-02");
		assertThat(error.getMessage()).isEqualTo("이미 존재하는 설정 키입니다.");
		assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("INVALID_SETTING_VALUE 에러 코드와 상태 검증")
	void invalidSettingValue_HasCorrectCodeAndStatus() {
		ConsoleError error = ConsoleError.INVALID_SETTING_VALUE;

		assertThat(error.getCode()).isEqualTo("CON-01-03");
		assertThat(error.getMessage()).isEqualTo("유효하지 않은 설정 값입니다.");
		assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("ANALYSIS_NOT_FOUND 에러 코드와 상태 검증")
	void analysisNotFound_HasCorrectCodeAndStatus() {
		ConsoleError error = ConsoleError.ANALYSIS_NOT_FOUND;

		assertThat(error.getCode()).isEqualTo("CON-02-01");
		assertThat(error.getMessage()).isEqualTo("분석 결과를 찾을 수 없습니다.");
		assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("ACTUATOR_CONNECTION_FAILED 에러 코드와 상태 검증")
	void actuatorConnectionFailed_HasCorrectCodeAndStatus() {
		ConsoleError error = ConsoleError.ACTUATOR_CONNECTION_FAILED;

		assertThat(error.getCode()).isEqualTo("CON-03-01");
		assertThat(error.getMessage()).isEqualTo("앱 서버 연결에 실패했습니다.");
		assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	@DisplayName("ACTUATOR_REQUEST_FAILED 에러 코드와 상태 검증")
	void actuatorRequestFailed_HasCorrectCodeAndStatus() {
		ConsoleError error = ConsoleError.ACTUATOR_REQUEST_FAILED;

		assertThat(error.getCode()).isEqualTo("CON-03-02");
		assertThat(error.getMessage()).isEqualTo("앱 서버 요청 처리 중 오류가 발생했습니다.");
		assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	@DisplayName("모든 ConsoleError 열거형이 6개임을 검증")
	void allValues_HasSixEntries() {
		assertThat(ConsoleError.values()).hasSize(6);
	}

	@Test
	@DisplayName("모든 에러 코드가 CON- 접두어로 시작")
	void allCodes_StartWithPrefix() {
		for (ConsoleError error : ConsoleError.values()) {
			assertThat(error.getCode()).startsWith("CON-");
		}
	}
}
