package com.aiinsightagent.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CommonError
		implements Error {
	INVALID_OPERATION_TYPE(CommonError.CODE_PREFIX + "01-10", "Unsupported license type", HttpStatus.BAD_REQUEST),
	INVALID_OPERATION_KEY(CommonError.CODE_PREFIX + "01-11", "Unsupported license key", HttpStatus.BAD_REQUEST),
	INVALID_EXPIRE_DATE_FORMAT(CommonError.CODE_PREFIX + "01-12", "Invalid expDate format (format: yyyy-MM-dd)", HttpStatus.BAD_REQUEST),

	EMPTY_FILE_INPUT_NULL(CommonError.CODE_PREFIX + "01-13", "File is null", HttpStatus.BAD_REQUEST),
	EMPTY_FILE(CommonError.CODE_PREFIX + "01-14", "File is empty : ", HttpStatus.BAD_REQUEST),
	INVALID_FILE_PATH(CommonError.CODE_PREFIX + "01-15", "Invalid File-path : ", HttpStatus.BAD_REQUEST),
	FAILED_READ_FILE(CommonError.CODE_PREFIX + "01-16", "Failed to read file : ", HttpStatus.BAD_REQUEST),
	INVALID_FILE_SIZE(CommonError.CODE_PREFIX + "01-17", "File size exceeded 10MB.", HttpStatus.BAD_REQUEST),
	INVALID_FILE_FORMAT(CommonError.CODE_PREFIX + "01-18", "File format not allowed : ", HttpStatus.BAD_REQUEST),
	INVALID_FILE_CONTENT(CommonError.CODE_PREFIX + "01-19" ,"The file does not contain any characters (only empty files or spaces).", HttpStatus.BAD_REQUEST),
	INVALID_FILE_STATUS_TYPE(CommonError.CODE_PREFIX + "01-20" ,"Invalid license Status type: ", HttpStatus.BAD_REQUEST),
	INVALID_FILE_PROCESS_TYPE(CommonError.CODE_PREFIX + "01-21" ,"Invalid license Status type: ", HttpStatus.BAD_REQUEST),
	FAILED_DATE_FORMAT_CONVERSION(CommonError.CODE_PREFIX + "01-22" ,"Date conversion failed: ", HttpStatus.BAD_REQUEST),

	COM_TEST(CommonError.CODE_PREFIX + "10-00", "테스트 예외가 발생하였습니다.", HttpStatus.BAD_REQUEST),
	COM_EMPTY_INPUT_DATA(CommonError.CODE_PREFIX + "10-01", "데이터가 비어 있습니다.", HttpStatus.BAD_REQUEST),
	COM_INVALID_ARGUMENT(CommonError.CODE_PREFIX + "10-02", "요청 값이 잘못 되었습니다.", HttpStatus.BAD_REQUEST),
	COM_ALREADY_EXISTS(CommonError.CODE_PREFIX + "10-03", "이미 해당 값이 존재합니다.", HttpStatus.BAD_REQUEST),

	COM_UNAUTHORIZED(CommonError.CODE_PREFIX + "20-01", "인증되지 않았습니다.", HttpStatus.UNAUTHORIZED),
	COM_NO_VALID_TOKEN(CommonError.CODE_PREFIX + "20-02", "유효한 토큰이 없거나 혀용되지 않는 IP입니다.", HttpStatus.UNAUTHORIZED),
	COM_FAILED_CREDENTIALS(CommonError.CODE_PREFIX + "20-03", "아이디 또는 비밀번호가 틀립니다.", HttpStatus.UNAUTHORIZED),
	COM_EXPIRED_TOKEN(CommonError.CODE_PREFIX + "20-05", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
	COM_NOT_SUPPORTED_TOKEN(CommonError.CODE_PREFIX + "20-06", "지원되지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED),
	COM_INVALID_TOKEN(CommonError.CODE_PREFIX + "20-07", "토큰이 잘못되었습니다.", HttpStatus.UNAUTHORIZED),

	COM_FORBIDDEN(CommonError.CODE_PREFIX + "30-01", "접근 권한이 없거나 혀용되지 않는 IP입니다.", HttpStatus.FORBIDDEN),
	COM_NOT_FOUND(CommonError.CODE_PREFIX + "30-02", "데이터를 찾을수 없습니다.", HttpStatus.NOT_FOUND),

	COM_ID_IS_NULL(CommonError.CODE_PREFIX + "40-01", "아이디가 비어 있습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_NO_ID_FIELD(CommonError.CODE_PREFIX + "40-02", "아이디가 정의되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_NOT_LOGGED_IN(CommonError.CODE_PREFIX + "40-03", "로그인하지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_NOT_SUPPORTED(CommonError.CODE_PREFIX + "40-04", "아직 지원하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_NOT_ACTIVATED(CommonError.CODE_PREFIX + "40-05", "사용중이지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_NOT_IMPLEMENTED(CommonError.CODE_PREFIX + "40-06", "구현되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	COM_INVALID_PASSWORD(CommonError.CODE_PREFIX + "50-01", "아이디 또는 비밀번호가 일치하지 않습니다.",
			HttpStatus.INTERNAL_SERVER_ERROR),
	COM_INVALID_NEW_PASSWORD(CommonError.CODE_PREFIX + "50-02", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.",
			HttpStatus.INTERNAL_SERVER_ERROR),
	COM_EXCEED_LOGIN_FAIL_COUNT(CommonError.CODE_PREFIX + "50-03", "로그인 실패 횟수를 초과했습니다.",
			HttpStatus.INTERNAL_SERVER_ERROR),

	COM_DB_ERROR(CommonError.CODE_PREFIX + "60-01", "DB 처리 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_IMAGING_ERROR(CommonError.CODE_PREFIX + "60-02", "이미지 처리 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_FILES_ERROR(CommonError.CODE_PREFIX + "60-03", "파일 처리 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_ENCRYPTION_ERROR(CommonError.CODE_PREFIX + "60-04", "암/복호화 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_EMAIL_ERROR(CommonError.CODE_PREFIX + "60-05", "이메일 발송 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_HTTP_CLIENT_ERROR(CommonError.CODE_PREFIX + "60-06", "HTTP 요청 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_READ_TEMPLETE_ERROR(CommonError.CODE_PREFIX + "60-07", "템플릿 파일을 읽을수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_JSON_ERROR(CommonError.CODE_PREFIX + "60-08", "JSON 처리 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	COM_SECURITY_ERROR(CommonError.CODE_PREFIX + "60-09", "보안 위반 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


	/**
	 * 공통 에러 코드 접두어
	 */
	public static final String CODE_PREFIX = "COM-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}
