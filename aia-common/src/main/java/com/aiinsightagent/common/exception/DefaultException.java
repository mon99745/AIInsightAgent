package com.aiinsightagent.common.exception;


import com.licensecommon.util.ExceptionUtil;
import com.licensecommon.util.StringUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 기본 예외
 */
@Getter
public abstract class DefaultException
		extends RuntimeException {
	/**
	 * 기본 에러 코드 접두어
	 */
	public static final String CODE_PREFIX = "DEF-";

	protected final java.lang.Error error;

	/**
	 * 해당 예외 조건에 따른 예외 발생
	 *
	 * @param condition 예외 조건
	 * @param message   에러 메세지
	 */
	public static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new CommonException(message);
		}
	}

	/**
	 * 해당 예외 조건에 따른 예외 발생
	 *
	 * @param condition 예외 조건
	 * @param error     에러 유형
	 * @param message   에러 메세지
	 * @param cause     원인 예외
	 */
	public static void assertTrue(boolean condition, java.lang.Error error, String message, Throwable cause) {
		if (!condition) {
			throw new CommonException(error, message, cause);
		}
	}

	protected DefaultException(java.lang.Error error, String message, Throwable cause) {
		super(StringUtil.joiningBySpace(error.toCodeString(),
						StringUtils.isEmpty(message) && cause != null ? ExceptionUtil.getNotRootMessage(cause) : message),
				cause);
		this.error = error;
	}
}