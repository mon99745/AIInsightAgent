package com.aiinsightagent.common.util;

import java.util.regex.Pattern;

public class IPCheckUtil {
	private IPCheckUtil() {
	}

	// IPv4 정규식
	private static final String IPV4_REGEX =
			"^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.|$)){4}$";
	private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

	// IPv6 정규식 (간단히 압축 표기 지원)
	private static final String IPV6_REGEX =
			"^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$" + // full form
					"|^(?:[0-9a-fA-F]{1,4}:){1,7}:$" +             // compressed ::
					"|^(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$" +
					"|^(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}$" +
					"|^(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}$" +
					"|^(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}$" +
					"|^(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}$" +
					"|^[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})$" +
					"|^:(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)$";
	private static final Pattern IPV6_PATTERN = Pattern.compile(IPV6_REGEX);

	/**
	 * 문자열이 IPv4인지 확인
	 */
	public static boolean isIPv4(String str) {
		return str != null && IPV4_PATTERN.matcher(str).matches();
	}

	/**
	 * 문자열이 IPv6인지 확인
	 */
	public static boolean isIPv6(String str) {
		return str != null && IPV6_PATTERN.matcher(str).matches();
	}

	/**
	 * 문자열이 IPv4 또는 IPv6인지 확인
	 */
	public static boolean isIP(String str) {
		return isIPv4(str) || isIPv6(str);
	}
}
