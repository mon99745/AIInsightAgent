package com.aiinsightagent.core.exception;


import com.aiinsightagent.common.exception.DefaultException;
import com.aiinsightagent.common.exception.Error;

public class InsightException
		extends DefaultException {
	public InsightException(String message) {
		this(message, null);
	}

	public InsightException(Throwable cause) {
		this((String) null, cause);
	}

	public InsightException(String message, Throwable cause) {
		this(Error.DefaultError.NONE, message, cause);
	}

	public InsightException(Error error) {
		this(error, (String) null);
	}

	public InsightException(Error error, String message) {
		this(error, message, null);
	}

	public InsightException(Error error, Throwable cause) {
		this(error, null, cause);
	}

	/**
	 * Insight 예외 생성자
	 *
	 * @param error   에러
	 * @param message 메세지
	 * @param cause   원인 예외
	 */
	public InsightException(Error error, String message, Throwable cause) {
		super(error, message, cause);
	}
}
