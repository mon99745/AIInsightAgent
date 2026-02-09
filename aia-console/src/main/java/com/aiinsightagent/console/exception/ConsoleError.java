package com.aiinsightagent.console.exception;

import com.aiinsightagent.common.exception.Error;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ConsoleError implements Error {

	SETTING_NOT_FOUND(ConsoleError.CODE_PREFIX + "01-01", "설정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	SETTING_KEY_DUPLICATE(ConsoleError.CODE_PREFIX + "01-02", "이미 존재하는 설정 키입니다.", HttpStatus.BAD_REQUEST),
	INVALID_SETTING_VALUE(ConsoleError.CODE_PREFIX + "01-03", "유효하지 않은 설정 값입니다.", HttpStatus.BAD_REQUEST),

	ANALYSIS_NOT_FOUND(ConsoleError.CODE_PREFIX + "02-01", "분석 결과를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	ACTUATOR_CONNECTION_FAILED(ConsoleError.CODE_PREFIX + "03-01", "앱 서버 연결에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
	ACTUATOR_REQUEST_FAILED(ConsoleError.CODE_PREFIX + "03-02", "앱 서버 요청 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

	public static final String CODE_PREFIX = "CON-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}
