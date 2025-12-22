package com.aiinsightagent.core.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.aiinsightagent.common.exception.Error;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum InsightError
		implements Error {

	BAD_REQUEST(InsightError.CODE_PREFIX + "01-00", "Bad Request", HttpStatus.BAD_REQUEST),


	INTERNAL_SERVER_ERROR(InsightError.CODE_PREFIX + "02-00", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

	public static final String CODE_PREFIX = "AIA-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}