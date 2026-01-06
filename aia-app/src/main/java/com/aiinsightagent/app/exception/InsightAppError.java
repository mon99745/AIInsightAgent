package com.aiinsightagent.app.exception;

import com.aiinsightagent.common.exception.Error;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum InsightAppError
		implements Error {

	BAD_REQUEST(InsightAppError.CODE_PREFIX + "01-00", "Bad Request", HttpStatus.BAD_REQUEST),
	MISSING_USER_PROMPT_REQUEST(InsightAppError.CODE_PREFIX + "01-01", "Missing exercise session data (userPrompt).", HttpStatus.BAD_REQUEST),
	EMPTY_DATA_KEY(InsightAppError.CODE_PREFIX + "01-02", "Each userPrompt must have a non-empty dataKey.", HttpStatus.BAD_REQUEST),
	EMPTY_DATA_OBJECT(InsightAppError.CODE_PREFIX + "01-03", "Each userPrompt must have a non-empty data object.", HttpStatus.BAD_REQUEST),

	INTERNAL_SERVER_ERROR(InsightAppError.CODE_PREFIX + "02-00", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
	FAIL_SERIALIZE_INSIGHT_DETAIL(InsightAppError.CODE_PREFIX + "02-01", "Failed to serialize InsightDetail", HttpStatus.INTERNAL_SERVER_ERROR),;

	public static final String CODE_PREFIX = "AIAA-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}