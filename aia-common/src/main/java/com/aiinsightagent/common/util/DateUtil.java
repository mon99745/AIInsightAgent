package com.aiinsightagent.common.util;

import com.aiinsightagent.common.exception.CommonError;
import com.aiinsightagent.common.exception.CommonException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static com.aiinsightagent.common.exception.CommonError.INVALID_EXPIRE_DATE_FORMAT;


@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {
	private static final String DEFAULT_FORMAT = "yyyy-MM-dd";

	public static Date parse(String dateStr) {
		return parse(dateStr, DEFAULT_FORMAT);
	}

	/**
	 * 문자열을 Date로 변환 (사용자 지정 포맷)
	 */
	public static Date parse(String dateStr, String format) {
		if (dateStr == null || dateStr.isBlank()) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			log.error("날짜 변환 실패: " + dateStr + ", 포맷: " + format);
			throw new CommonException(CommonError.FAILED_DATE_FORMAT_CONVERSION, dateStr);
		}
	}

	/**
	 * Date를 문자열로 변환 (기본 포맷)
	 */
	public static String format(Date date) {
		return format(date, DEFAULT_FORMAT);
	}

	/**
	 * Date를 문자열로 변환 (사용자 지정 포맷)
	 */
	public static String format(Date date, String format) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * expDate 문자열이 지정된 날짜 포맷에 맞는지 검증
	 *
	 * @param expDate 날짜 문자열
	 * @return 포맷에 맞는 날짜
	 */
	public static LocalDate isValidDateFormat(String expDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_FORMAT);
		LocalDate parsedDate;

		try {
			parsedDate = LocalDate.parse(expDate, formatter);
			return parsedDate;
		} catch (DateTimeParseException e) {
			throw new CommonException(INVALID_EXPIRE_DATE_FORMAT);
		}
	}
}