package com.aiinsightagent.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class CommonErrorTest {

	@Nested
	@DisplayName("에러 코드 검증 테스트")
	class ErrorCodeTest {

		@Test
		@DisplayName("에러 코드가 COM- 접두어로 시작한다")
		void errorCode_startsWithComPrefix() {
			// then
			for (CommonError error : CommonError.values()) {
				assertThat(error.getCode()).startsWith(CommonError.CODE_PREFIX);
			}
		}

		@Test
		@DisplayName("CODE_PREFIX가 COM-이다")
		void codePrefix_isCom() {
			assertThat(CommonError.CODE_PREFIX).isEqualTo("COM-");
		}

		@Test
		@DisplayName("COM_INVALID_ARGUMENT 에러 코드가 올바르다")
		void comInvalidArgument_hasCorrectCode() {
			assertThat(CommonError.COM_INVALID_ARGUMENT.getCode()).isEqualTo("COM-10-02");
		}

		@Test
		@DisplayName("COM_NOT_FOUND 에러 코드가 올바르다")
		void comNotFound_hasCorrectCode() {
			assertThat(CommonError.COM_NOT_FOUND.getCode()).isEqualTo("COM-30-02");
		}
	}

	@Nested
	@DisplayName("에러 메시지 검증 테스트")
	class ErrorMessageTest {

		@Test
		@DisplayName("모든 에러가 메시지를 가지고 있다")
		void allErrors_haveMessages() {
			for (CommonError error : CommonError.values()) {
				assertThat(error.getMessage()).isNotNull();
			}
		}

		@Test
		@DisplayName("COM_INVALID_ARGUMENT 에러 메시지가 올바르다")
		void comInvalidArgument_hasCorrectMessage() {
			assertThat(CommonError.COM_INVALID_ARGUMENT.getMessage())
					.isEqualTo("요청 값이 잘못 되었습니다.");
		}

		@Test
		@DisplayName("INVALID_EXPIRE_DATE_FORMAT 에러 메시지가 올바르다")
		void invalidExpireDateFormat_hasCorrectMessage() {
			assertThat(CommonError.INVALID_EXPIRE_DATE_FORMAT.getMessage())
					.isEqualTo("Invalid expDate format (format: yyyy-MM-dd)");
		}
	}

	@Nested
	@DisplayName("HTTP 상태 코드 검증 테스트")
	class HttpStatusTest {

		@Test
		@DisplayName("모든 에러가 HTTP 상태를 가지고 있다")
		void allErrors_haveHttpStatus() {
			for (CommonError error : CommonError.values()) {
				assertThat(error.getHttpStatus()).isNotNull();
			}
		}

		@Test
		@DisplayName("COM_INVALID_ARGUMENT는 BAD_REQUEST 상태이다")
		void comInvalidArgument_isBadRequest() {
			assertThat(CommonError.COM_INVALID_ARGUMENT.getHttpStatus())
					.isEqualTo(HttpStatus.BAD_REQUEST);
		}

		@Test
		@DisplayName("COM_UNAUTHORIZED는 UNAUTHORIZED 상태이다")
		void comUnauthorized_isUnauthorized() {
			assertThat(CommonError.COM_UNAUTHORIZED.getHttpStatus())
					.isEqualTo(HttpStatus.UNAUTHORIZED);
		}

		@Test
		@DisplayName("COM_FORBIDDEN는 FORBIDDEN 상태이다")
		void comForbidden_isForbidden() {
			assertThat(CommonError.COM_FORBIDDEN.getHttpStatus())
					.isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("COM_NOT_FOUND는 NOT_FOUND 상태이다")
		void comNotFound_isNotFound() {
			assertThat(CommonError.COM_NOT_FOUND.getHttpStatus())
					.isEqualTo(HttpStatus.NOT_FOUND);
		}

		@Test
		@DisplayName("COM_DB_ERROR는 INTERNAL_SERVER_ERROR 상태이다")
		void comDbError_isInternalServerError() {
			assertThat(CommonError.COM_DB_ERROR.getHttpStatus())
					.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Nested
	@DisplayName("toString 테스트")
	class ToStringTest {

		@Test
		@DisplayName("toString은 toCodeString 결과를 반환한다")
		void toString_returnsCodeString() {
			// given
			CommonError error = CommonError.COM_INVALID_ARGUMENT;

			// when
			String toStringResult = error.toString();
			String toCodeStringResult = error.toCodeString();

			// then
			assertThat(toStringResult).isEqualTo(toCodeStringResult);
		}

		@Test
		@DisplayName("toString 결과에 메시지가 포함된다")
		void toString_containsMessage() {
			// given
			CommonError error = CommonError.COM_NOT_FOUND;

			// when
			String result = error.toString();

			// then
			assertThat(result).contains(error.getMessage());
		}
	}

	@Nested
	@DisplayName("Error 인터페이스 구현 테스트")
	class ErrorInterfaceTest {

		@Test
		@DisplayName("CommonError는 Error 인터페이스를 구현한다")
		void commonError_implementsError() {
			// given
			CommonError error = CommonError.COM_TEST;

			// then
			assertThat(error).isInstanceOf(Error.class);
		}

		@Test
		@DisplayName("name() 메서드가 enum 이름을 반환한다")
		void name_returnsEnumName() {
			assertThat(CommonError.COM_TEST.name()).isEqualTo("COM_TEST");
		}

		@Test
		@DisplayName("ofString() 메서드가 상세 정보를 반환한다")
		void ofString_returnsDetailedInfo() {
			// given
			CommonError error = CommonError.COM_INVALID_ARGUMENT;

			// when
			String result = error.ofString();

			// then
			assertThat(result).contains("name: COM_INVALID_ARGUMENT");
			assertThat(result).contains("code: COM-10-02");
			assertThat(result).contains("httpStatus: 400 BAD_REQUEST");
		}
	}

	@Nested
	@DisplayName("에러 분류 테스트")
	class ErrorCategoryTest {

		@Test
		@DisplayName("파일 관련 에러들이 존재한다")
		void fileErrors_exist() {
			assertThat(CommonError.EMPTY_FILE_INPUT_NULL).isNotNull();
			assertThat(CommonError.EMPTY_FILE).isNotNull();
			assertThat(CommonError.INVALID_FILE_PATH).isNotNull();
			assertThat(CommonError.FAILED_READ_FILE).isNotNull();
			assertThat(CommonError.INVALID_FILE_SIZE).isNotNull();
			assertThat(CommonError.INVALID_FILE_FORMAT).isNotNull();
		}

		@Test
		@DisplayName("인증 관련 에러들이 존재한다")
		void authErrors_exist() {
			assertThat(CommonError.COM_UNAUTHORIZED).isNotNull();
			assertThat(CommonError.COM_NO_VALID_TOKEN).isNotNull();
			assertThat(CommonError.COM_FAILED_CREDENTIALS).isNotNull();
			assertThat(CommonError.COM_EXPIRED_TOKEN).isNotNull();
			assertThat(CommonError.COM_INVALID_TOKEN).isNotNull();
		}

		@Test
		@DisplayName("비밀번호 관련 에러들이 존재한다")
		void passwordErrors_exist() {
			assertThat(CommonError.COM_INVALID_PASSWORD).isNotNull();
			assertThat(CommonError.COM_INVALID_NEW_PASSWORD).isNotNull();
			assertThat(CommonError.COM_EXCEED_LOGIN_FAIL_COUNT).isNotNull();
		}
	}
}
