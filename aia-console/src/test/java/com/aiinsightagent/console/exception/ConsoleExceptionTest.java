package com.aiinsightagent.console.exception;

import com.aiinsightagent.common.exception.Error;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleException 테스트")
class ConsoleExceptionTest {

	@Test
	@DisplayName("메시지만으로 생성")
	void constructor_WithMessage() {
		ConsoleException ex = new ConsoleException("test error");

		assertThat(ex.getMessage()).contains("test error");
		assertThat(ex.getError()).isEqualTo(Error.DefaultError.NONE);
		assertThat(ex.getCause()).isNull();
	}

	@Test
	@DisplayName("원인 예외로 생성")
	void constructor_WithCause() {
		RuntimeException cause = new RuntimeException("root cause");
		ConsoleException ex = new ConsoleException(cause);

		assertThat(ex.getCause()).isEqualTo(cause);
		assertThat(ex.getError()).isEqualTo(Error.DefaultError.NONE);
	}

	@Test
	@DisplayName("메시지와 원인 예외로 생성")
	void constructor_WithMessageAndCause() {
		RuntimeException cause = new RuntimeException("root cause");
		ConsoleException ex = new ConsoleException("test error", cause);

		assertThat(ex.getMessage()).contains("test error");
		assertThat(ex.getCause()).isEqualTo(cause);
	}

	@Test
	@DisplayName("Error로 생성")
	void constructor_WithError() {
		ConsoleException ex = new ConsoleException(ConsoleError.ANALYSIS_NOT_FOUND);

		assertThat(ex.getError()).isEqualTo(ConsoleError.ANALYSIS_NOT_FOUND);
	}

	@Test
	@DisplayName("Error와 메시지로 생성")
	void constructor_WithErrorAndMessage() {
		ConsoleException ex = new ConsoleException(ConsoleError.SETTING_NOT_FOUND, "추가 정보");

		assertThat(ex.getError()).isEqualTo(ConsoleError.SETTING_NOT_FOUND);
		assertThat(ex.getMessage()).contains("추가 정보");
	}

	@Test
	@DisplayName("Error와 원인 예외로 생성")
	void constructor_WithErrorAndCause() {
		RuntimeException cause = new RuntimeException("root");
		ConsoleException ex = new ConsoleException(ConsoleError.ACTUATOR_CONNECTION_FAILED, cause);

		assertThat(ex.getError()).isEqualTo(ConsoleError.ACTUATOR_CONNECTION_FAILED);
		assertThat(ex.getCause()).isEqualTo(cause);
	}

	@Test
	@DisplayName("Error, 메시지, 원인 예외로 생성")
	void constructor_WithErrorMessageAndCause() {
		RuntimeException cause = new RuntimeException("root");
		ConsoleException ex = new ConsoleException(ConsoleError.ACTUATOR_REQUEST_FAILED, "상세 정보", cause);

		assertThat(ex.getError()).isEqualTo(ConsoleError.ACTUATOR_REQUEST_FAILED);
		assertThat(ex.getMessage()).contains("상세 정보");
		assertThat(ex.getCause()).isEqualTo(cause);
	}
}
