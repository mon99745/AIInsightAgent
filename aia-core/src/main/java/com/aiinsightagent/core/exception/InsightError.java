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
	EMPTY_USER_PROMPT(InsightError.CODE_PREFIX + "01-00", "UserPrompt is required", HttpStatus.BAD_REQUEST),

	INTERNAL_SERVER_ERROR(InsightError.CODE_PREFIX + "02-00", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
	PROMPT_COMPOSITION_FAILURE(InsightError.CODE_PREFIX + "02-01", "Failed to convert Prompt to JSON string", HttpStatus.INTERNAL_SERVER_ERROR),
	EMPTY_GEMINI_RESPONSE(InsightError.CODE_PREFIX + "02-02", "Gemini returned empty response", HttpStatus.INTERNAL_SERVER_ERROR),
	FAIL_JSON_PARSING(InsightError.CODE_PREFIX + "02-03", "Failed to parse JSON", HttpStatus.INTERNAL_SERVER_ERROR);

	public static final String CODE_PREFIX = "AIAC-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}