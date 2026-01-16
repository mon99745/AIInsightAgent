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
	EMPTY_USER_PROMPT(InsightError.CODE_PREFIX + "01-01", "UserPrompt is required", HttpStatus.BAD_REQUEST),
	EXIST_ACTOR_PREPARED_CONTEXT(InsightError.CODE_PREFIX + "01-02", "Prepared context already exists for actor", HttpStatus.BAD_REQUEST),
	EMPTY_ACTOR_PREPARED_CONTEXT(InsightError.CODE_PREFIX + "01-03", "No prepared context for actor", HttpStatus.BAD_REQUEST),
	NOT_FOUND_ACTOR(InsightError.CODE_PREFIX + "01-03", "Actor not found", HttpStatus.BAD_REQUEST),


	INTERNAL_SERVER_ERROR(InsightError.CODE_PREFIX + "02-00", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
	PROMPT_COMPOSITION_FAILURE(InsightError.CODE_PREFIX + "02-01", "Failed to convert Prompt to JSON string", HttpStatus.INTERNAL_SERVER_ERROR),
	EMPTY_GEMINI_RESPONSE(InsightError.CODE_PREFIX + "02-02", "Gemini returned empty response", HttpStatus.INTERNAL_SERVER_ERROR),
	FAIL_JSON_PARSING(InsightError.CODE_PREFIX + "02-03", "Failed to parse JSON", HttpStatus.INTERNAL_SERVER_ERROR),
	RESPONSE_TRUNCATED(InsightError.CODE_PREFIX + "02-04", "Response truncated due to max output tokens limit. Increase max-output-tokens value.", HttpStatus.INTERNAL_SERVER_ERROR),

	QUEUE_FULL(InsightError.CODE_PREFIX + "03-01", "Request queue is full", HttpStatus.SERVICE_UNAVAILABLE),
	QUEUE_TIMEOUT(InsightError.CODE_PREFIX + "03-02", "Request timed out in queue", HttpStatus.GATEWAY_TIMEOUT),
	QUEUE_NOT_RUNNING(InsightError.CODE_PREFIX + "03-03", "Queue manager is not running", HttpStatus.SERVICE_UNAVAILABLE),

	GEMINI_RATE_LIMIT(InsightError.CODE_PREFIX + "04-01", "Gemini API rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);

	public static final String CODE_PREFIX = "AIAC-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}