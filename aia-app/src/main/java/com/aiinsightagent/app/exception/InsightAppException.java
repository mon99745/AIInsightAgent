package com.aiinsightagent.app.exception;


import com.aiinsightagent.common.exception.DefaultException;
import com.aiinsightagent.common.exception.Error;

public class InsightAppException
		extends DefaultException {
	public InsightAppException(String message) {
		this(message, null);
	}

	public InsightAppException(Throwable cause) {
		this((String) null, cause);
	}

	public InsightAppException(String message, Throwable cause) {
		this(Error.DefaultError.NONE, message, cause);
	}

	public InsightAppException(Error error) {
		this(error, (String) null);
	}

	public InsightAppException(Error error, String message) {
		this(error, message, null);
	}

	public InsightAppException(Error error, Throwable cause) {
		this(error, null, cause);
	}

	/**
	 * Insight App 예외 생성자
	 *
	 * @param error   에러
	 * @param message 메세지
	 * @param cause   원인 예외
	 */
	public InsightAppException(Error error, String message, Throwable cause) {
		super(error, message, cause);
	}
}
