package com.aiinsightagent.console.exception;

import com.aiinsightagent.common.exception.DefaultException;
import com.aiinsightagent.common.exception.Error;

public class ConsoleException extends DefaultException {

	public ConsoleException(String message) {
		this(message, null);
	}

	public ConsoleException(Throwable cause) {
		this((String) null, cause);
	}

	public ConsoleException(String message, Throwable cause) {
		this(Error.DefaultError.NONE, message, cause);
	}

	public ConsoleException(Error error) {
		this(error, (String) null);
	}

	public ConsoleException(Error error, String message) {
		this(error, message, null);
	}

	public ConsoleException(Error error, Throwable cause) {
		this(error, null, cause);
	}

	public ConsoleException(Error error, String message, Throwable cause) {
		super(error, message, cause);
	}
}
