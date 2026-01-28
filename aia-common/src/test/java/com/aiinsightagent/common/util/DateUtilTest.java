package com.aiinsightagent.common.util;

import com.aiinsightagent.common.exception.CommonException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateUtilTest {

	@Nested
	@DisplayName("parse(String) 테스트")
	class ParseDefaultFormatTest {

		@Test
		@DisplayName("기본 포맷(yyyy-MM-dd)으로 날짜를 파싱한다")
		void parse_validDate_returnsDate() {
			// given
			String dateStr = "2024-01-15";

			// when
			Date result = DateUtil.parse(dateStr);

			// then
			assertThat(result).isNotNull();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			assertThat(sdf.format(result)).isEqualTo("2024-01-15");
		}

		@Test
		@DisplayName("null 입력 시 null을 반환한다")
		void parse_nullInput_returnsNull() {
			// when
			Date result = DateUtil.parse(null);

			// then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("빈 문자열 입력 시 null을 반환한다")
		void parse_emptyInput_returnsNull() {
			// when
			Date result = DateUtil.parse("");

			// then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("공백 문자열 입력 시 null을 반환한다")
		void parse_blankInput_returnsNull() {
			// when
			Date result = DateUtil.parse("   ");

			// then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("잘못된 형식의 날짜 입력 시 예외를 발생시킨다")
		void parse_invalidFormat_throwsException() {
			// given
			String invalidDate = "invalid-date-string";

			// when & then
			assertThatThrownBy(() -> DateUtil.parse(invalidDate))
					.isInstanceOf(CommonException.class);
		}
	}

	@Nested
	@DisplayName("parse(String, String) 테스트")
	class ParseCustomFormatTest {

		@Test
		@DisplayName("커스텀 포맷으로 날짜를 파싱한다")
		void parse_customFormat_returnsDate() {
			// given
			String dateStr = "15/01/2024";
			String format = "dd/MM/yyyy";

			// when
			Date result = DateUtil.parse(dateStr, format);

			// then
			assertThat(result).isNotNull();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			assertThat(sdf.format(result)).isEqualTo("2024-01-15");
		}

		@Test
		@DisplayName("yyyyMMdd 포맷으로 날짜를 파싱한다")
		void parse_yyyyMMddFormat_returnsDate() {
			// given
			String dateStr = "20240115";
			String format = "yyyyMMdd";

			// when
			Date result = DateUtil.parse(dateStr, format);

			// then
			assertThat(result).isNotNull();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			assertThat(sdf.format(result)).isEqualTo("2024-01-15");
		}

		@Test
		@DisplayName("커스텀 포맷과 일치하지 않는 날짜 입력 시 예외를 발생시킨다")
		void parse_formatMismatch_throwsException() {
			// given
			String dateStr = "2024-01-15";
			String format = "dd/MM/yyyy";

			// when & then
			assertThatThrownBy(() -> DateUtil.parse(dateStr, format))
					.isInstanceOf(CommonException.class);
		}
	}

	@Nested
	@DisplayName("format(Date) 테스트")
	class FormatDefaultTest {

		@Test
		@DisplayName("Date를 기본 포맷(yyyy-MM-dd)으로 변환한다")
		void format_validDate_returnsFormattedString() throws Exception {
			// given
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse("2024-01-15");

			// when
			String result = DateUtil.format(date);

			// then
			assertThat(result).isEqualTo("2024-01-15");
		}

		@Test
		@DisplayName("null 입력 시 null을 반환한다")
		void format_nullInput_returnsNull() {
			// when
			String result = DateUtil.format(null);

			// then
			assertThat(result).isNull();
		}
	}

	@Nested
	@DisplayName("format(Date, String) 테스트")
	class FormatCustomTest {

		@Test
		@DisplayName("커스텀 포맷으로 Date를 문자열로 변환한다")
		void format_customFormat_returnsFormattedString() throws Exception {
			// given
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse("2024-01-15");
			String format = "dd/MM/yyyy";

			// when
			String result = DateUtil.format(date, format);

			// then
			assertThat(result).isEqualTo("15/01/2024");
		}

		@Test
		@DisplayName("yyyyMMdd 포맷으로 Date를 문자열로 변환한다")
		void format_yyyyMMddFormat_returnsFormattedString() throws Exception {
			// given
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse("2024-01-15");
			String format = "yyyyMMdd";

			// when
			String result = DateUtil.format(date, format);

			// then
			assertThat(result).isEqualTo("20240115");
		}

		@Test
		@DisplayName("null Date 입력 시 null을 반환한다")
		void format_nullDate_returnsNull() {
			// when
			String result = DateUtil.format(null, "yyyy-MM-dd");

			// then
			assertThat(result).isNull();
		}
	}

	@Nested
	@DisplayName("isValidDateFormat 테스트")
	class IsValidDateFormatTest {

		@Test
		@DisplayName("유효한 날짜 형식을 검증한다")
		void isValidDateFormat_validDate_returnsLocalDate() {
			// given
			String expDate = "2024-01-15";

			// when
			LocalDate result = DateUtil.isValidDateFormat(expDate);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getYear()).isEqualTo(2024);
			assertThat(result.getMonthValue()).isEqualTo(1);
			assertThat(result.getDayOfMonth()).isEqualTo(15);
		}

		@Test
		@DisplayName("유효하지 않은 날짜 형식 입력 시 예외를 발생시킨다")
		void isValidDateFormat_invalidFormat_throwsException() {
			// given
			String invalidDate = "15-01-2024";

			// when & then
			assertThatThrownBy(() -> DateUtil.isValidDateFormat(invalidDate))
					.isInstanceOf(CommonException.class);
		}

		@Test
		@DisplayName("완전히 잘못된 날짜 형식 입력 시 예외를 발생시킨다")
		void isValidDateFormat_completelyInvalidDate_throwsException() {
			// given
			String invalidDate = "abc-de-fg";

			// when & then
			assertThatThrownBy(() -> DateUtil.isValidDateFormat(invalidDate))
					.isInstanceOf(CommonException.class);
		}

		@Test
		@DisplayName("잘못된 월 값 입력 시 예외를 발생시킨다")
		void isValidDateFormat_invalidMonth_throwsException() {
			// given
			String invalidDate = "2024-13-01";

			// when & then
			assertThatThrownBy(() -> DateUtil.isValidDateFormat(invalidDate))
					.isInstanceOf(CommonException.class);
		}
	}
}
