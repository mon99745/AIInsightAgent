package com.aiinsightagent.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultExceptionTest {

	@Nested
	@DisplayName("assertTrue(boolean, String) 테스트")
	class AssertTrueWithMessageTest {

		@Test
		@DisplayName("조건이 true이면 예외를 발생시키지 않는다")
		void assertTrue_trueCondition_noException() {
			// when & then - no exception
			DefaultException.assertTrue(true, "에러 메시지");
		}

		@Test
		@DisplayName("조건이 false이면 CommonException을 발생시킨다")
		void assertTrue_falseCondition_throwsCommonException() {
			// when & then
			assertThatThrownBy(() -> DefaultException.assertTrue(false, "에러 메시지"))
					.isInstanceOf(CommonException.class);
		}

		@Test
		@DisplayName("발생한 예외에 메시지가 포함된다")
		void assertTrue_falseCondition_containsMessage() {
			// when & then
			assertThatThrownBy(() -> DefaultException.assertTrue(false, "테스트 에러 메시지"))
					.isInstanceOf(CommonException.class)
					.hasMessageContaining("테스트 에러 메시지");
		}
	}

	@Nested
	@DisplayName("assertTrue(boolean, Error, String, Throwable) 테스트")
	class AssertTrueWithErrorTest {

		@Test
		@DisplayName("조건이 true이면 예외를 발생시키지 않는다")
		void assertTrue_trueCondition_noException() {
			// given
			Error error = CommonError.COM_INVALID_ARGUMENT;

			// when & then - no exception
			DefaultException.assertTrue(true, error, "추가 메시지", null);
		}

		@Test
		@DisplayName("조건이 false이면 Error 정보와 함께 CommonException을 발생시킨다")
		void assertTrue_falseCondition_throwsCommonExceptionWithError() {
			// given
			Error error = CommonError.COM_INVALID_ARGUMENT;

			// when & then
			assertThatThrownBy(() -> DefaultException.assertTrue(false, error, "추가 메시지", null))
					.isInstanceOf(CommonException.class);
		}

		@Test
		@DisplayName("원인 예외가 함께 전달된다")
		void assertTrue_withCause_includesCause() {
			// given
			Error error = CommonError.COM_INVALID_ARGUMENT;
			RuntimeException cause = new RuntimeException("원인 예외");

			// when & then
			assertThatThrownBy(() -> DefaultException.assertTrue(false, error, "메시지", cause))
					.isInstanceOf(CommonException.class)
					.hasCause(cause);
		}

		@Test
		@DisplayName("null 원인도 허용된다")
		void assertTrue_nullCause_works() {
			// given
			Error error = CommonError.COM_INVALID_ARGUMENT;

			// when & then
			assertThatThrownBy(() -> DefaultException.assertTrue(false, error, "메시지", null))
					.isInstanceOf(CommonException.class)
					.hasNoCause();
		}
	}

	@Nested
	@DisplayName("CODE_PREFIX 테스트")
	class CodePrefixTest {

		@Test
		@DisplayName("CODE_PREFIX가 DEF-이다")
		void codePrefix_isDef() {
			assertThat(DefaultException.CODE_PREFIX).isEqualTo("DEF-");
		}
	}

	@Nested
	@DisplayName("getError 테스트")
	class GetErrorTest {

		@Test
		@DisplayName("생성 시 전달한 Error를 반환한다")
		void getError_returnsProvidedError() {
			// given
			CommonException ex = new CommonException(CommonError.COM_INVALID_ARGUMENT);

			// when
			Error result = ex.getError();

			// then
			assertThat(result).isEqualTo(CommonError.COM_INVALID_ARGUMENT);
		}

		@Test
		@DisplayName("문자열만으로 생성 시 DefaultError.NONE을 반환한다")
		void getError_stringOnly_returnsNone() {
			// given
			CommonException ex = new CommonException("에러 메시지");

			// when
			Error result = ex.getError();

			// then
			assertThat(result).isEqualTo(Error.DefaultError.NONE);
		}
	}

	@Nested
	@DisplayName("예외 메시지 구성 테스트")
	class ExceptionMessageTest {

		@Test
		@DisplayName("Error와 메시지가 조합되어 메시지를 구성한다")
		void message_combinesErrorAndMessage() {
			// given
			CommonException ex = new CommonException(CommonError.COM_INVALID_ARGUMENT, "상세 설명");

			// when
			String message = ex.getMessage();

			// then
			assertThat(message).contains(CommonError.COM_INVALID_ARGUMENT.getMessage());
		}

		@Test
		@DisplayName("원인 예외가 있을 때 메시지가 구성된다")
		void message_withCause_isConstructed() {
			// given
			RuntimeException cause = new RuntimeException("원인 예외");
			CommonException ex = new CommonException(CommonError.COM_INVALID_ARGUMENT, cause);

			// when
			String message = ex.getMessage();

			// then
			assertThat(message).isNotNull();
			assertThat(ex.getCause()).isEqualTo(cause);
		}
	}

	@Nested
	@DisplayName("Error.DefaultError 테스트")
	class DefaultErrorEnumTest {

		@Test
		@DisplayName("NONE 에러가 존재한다")
		void none_exists() {
			assertThat(Error.DefaultError.NONE).isNotNull();
			assertThat(Error.DefaultError.NONE.getCode()).isEqualTo("DEF-10-01");
		}

		@Test
		@DisplayName("UNKNOWN 에러가 존재한다")
		void unknown_exists() {
			assertThat(Error.DefaultError.UNKNOWN).isNotNull();
			assertThat(Error.DefaultError.UNKNOWN.getCode()).isEqualTo("DEF-10-02");
			assertThat(Error.DefaultError.UNKNOWN.getMessage()).isEqualTo("알수 없음");
		}

		@Test
		@DisplayName("DefaultError는 INTERNAL_SERVER_ERROR 상태를 가진다")
		void defaultError_hasInternalServerErrorStatus() {
			assertThat(Error.DefaultError.NONE.getHttpStatus())
					.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(Error.DefaultError.UNKNOWN.getHttpStatus())
					.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		@Test
		@DisplayName("toString은 toCodeString 결과를 반환한다")
		void toString_returnsCodeString() {
			// given
			Error.DefaultError error = Error.DefaultError.UNKNOWN;

			// when
			String toStringResult = error.toString();
			String toCodeStringResult = error.toCodeString();

			// then
			assertThat(toStringResult).isEqualTo(toCodeStringResult);
		}
	}

	@Nested
	@DisplayName("Error.CustomError 테스트")
	class CustomErrorTest {

		@Test
		@DisplayName("createError로 CustomError를 생성할 수 있다")
		void createError_createsCustomError() {
			// when
			Error.CustomError customError = Error.CustomError.createError(
					"CUSTOM_NAME",
					"CUSTOM-001",
					"커스텀 에러 메시지",
					HttpStatus.BAD_REQUEST
			);

			// then
			assertThat(customError.name()).isEqualTo("CUSTOM_NAME");
			assertThat(customError.getCode()).isEqualTo("CUSTOM-001");
			assertThat(customError.getMessage()).isEqualTo("커스텀 에러 메시지");
			assertThat(customError.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}

		@Test
		@DisplayName("기존 Error로부터 CustomError를 생성할 수 있다")
		void createError_fromExistingError() {
			// given
			Error existingError = CommonError.COM_INVALID_ARGUMENT;

			// when
			Error.CustomError customError = Error.CustomError.createError(
					existingError,
					HttpStatus.UNPROCESSABLE_ENTITY
			);

			// then
			assertThat(customError.name()).isEqualTo(existingError.name());
			assertThat(customError.getCode()).isEqualTo(existingError.getCode());
			assertThat(customError.getMessage()).isEqualTo(existingError.getMessage());
			assertThat(customError.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		}

		@Test
		@DisplayName("CustomError의 toString은 toCodeString 결과를 반환한다")
		void toString_returnsCodeString() {
			// given
			Error.CustomError customError = Error.CustomError.createError(
					"TEST", "TEST-001", "테스트", HttpStatus.OK
			);

			// when
			String toStringResult = customError.toString();
			String toCodeStringResult = customError.toCodeString();

			// then
			assertThat(toStringResult).isEqualTo(toCodeStringResult);
		}
	}
}
