package com.aiinsightagent.common.exception;

/**
 * 공통 예외
 */
public class CommonException
		extends DefaultException {
	public CommonException(String message) {
		this(message, null);
	}

	public CommonException(Throwable cause) {
		this((String) null, cause);
	}

	public CommonException(String message, Throwable cause) {
		this(java.lang.Error.DefaultError.NONE, message, cause);
	}

	public CommonException(java.lang.Error error) {
		this(error, (String) null);
	}

	public CommonException(java.lang.Error error, String message) {
		this(error, message, null);
	}

	public CommonException(java.lang.Error error, Throwable cause) {
		this(error, null, cause);
	}

	public CommonException(java.lang.Error error, String message, Throwable cause) {
		super(error, message, cause);
	}
}