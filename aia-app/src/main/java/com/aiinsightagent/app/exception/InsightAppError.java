package com.aiinsightagent.app.exception;

import com.aiinsightagent.common.exception.Error;
import com.aiinsightagent.core.exception.InsightError;
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
	EMPTY_USER_ID(InsightAppError.CODE_PREFIX + "01-04", "Empty or Null, User ID.", HttpStatus.BAD_REQUEST),
	EMPTY_PURPOSE(InsightAppError.CODE_PREFIX + "01-05", "Empty or Null, Purpose.", HttpStatus.BAD_REQUEST),
	EMPTY_CONTEXT(InsightAppError.CODE_PREFIX + "01-06", "Empty or Null, Context.", HttpStatus.BAD_REQUEST),
	EMPTY_CONTEXT_CATEGORY(InsightAppError.CODE_PREFIX + "01-07", "Empty or Null, Context Category.", HttpStatus.BAD_REQUEST),
	EMPTY_CONTEXT_DATA(InsightAppError.CODE_PREFIX + "01-08", "Empty or Null, Context data.", HttpStatus.BAD_REQUEST),

	INTERNAL_SERVER_ERROR(InsightAppError.CODE_PREFIX + "02-00", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
	FAIL_SERIALIZE_INSIGHT_DETAIL(InsightAppError.CODE_PREFIX + "02-01", "Failed to serialize InsightDetail", HttpStatus.INTERNAL_SERVER_ERROR),
	FAIL_JSON_PARSING_RAW_DATA(InsightError.CODE_PREFIX + "02-02", "Failed to parse rawData", HttpStatus.INTERNAL_SERVER_ERROR),
	FAIL_JSON_SERIALIZATION(InsightError.CODE_PREFIX + "02-03", "Failed to serialize userPrompts to JSON", HttpStatus.INTERNAL_SERVER_ERROR);

	public static final String CODE_PREFIX = "AIAA-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}