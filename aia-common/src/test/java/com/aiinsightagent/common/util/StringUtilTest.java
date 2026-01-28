package com.aiinsightagent.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {

	@Nested
	@DisplayName("toCodeString 테스트")
	class ToCodeStringTest {

		@Test
		@DisplayName("코드와 메시지를 [code]message 형식으로 변환한다")
		void toCodeString_validInput_returnsFormattedString() {
			// given
			String code = "ERR-001";
			String message = "에러가 발생했습니다";

			// when
			String result = StringUtil.toCodeString(code, message);

			// then
			assertThat(result).isEqualTo("[ERR-001]에러가 발생했습니다");
		}

		@Test
		@DisplayName("숫자 코드도 처리할 수 있다")
		void toCodeString_numericCode_returnsFormattedString() {
			// given
			Integer code = 500;
			String message = "Internal Server Error";

			// when
			String result = StringUtil.toCodeString(code, message);

			// then
			assertThat(result).isEqualTo("[500]Internal Server Error");
		}

		@Test
		@DisplayName("빈 메시지도 처리할 수 있다")
		void toCodeString_emptyMessage_returnsFormattedString() {
			// given
			String code = "WARN";
			String message = "";

			// when
			String result = StringUtil.toCodeString(code, message);

			// then
			assertThat(result).isEqualTo("[WARN]");
		}

		@Test
		@DisplayName("null 값도 처리할 수 있다")
		void toCodeString_nullValues_returnsFormattedString() {
			// when
			String result = StringUtil.toCodeString(null, null);

			// then
			assertThat(result).isEqualTo("[null]null");
		}
	}

	@Nested
	@DisplayName("joiningBySpace 테스트")
	class JoiningBySpaceTest {

		@Test
		@DisplayName("문자열들을 공백으로 결합한다")
		void joiningBySpace_multipleStrings_returnsSpaceSeparated() {
			// when
			String result = StringUtil.joiningBySpace("Hello", "World", "Test");

			// then
			assertThat(result).isEqualTo("Hello World Test");
		}

		@Test
		@DisplayName("단일 문자열은 그대로 반환한다")
		void joiningBySpace_singleString_returnsSameString() {
			// when
			String result = StringUtil.joiningBySpace("Hello");

			// then
			assertThat(result).isEqualTo("Hello");
		}

		@Test
		@DisplayName("빈 문자열은 결과에서 제외된다")
		void joiningBySpace_withEmptyStrings_excludesEmpty() {
			// when
			String result = StringUtil.joiningBySpace("Hello", "", "World");

			// then
			assertThat(result).isEqualTo("Hello World");
		}

		@Test
		@DisplayName("null 값은 결과에서 제외된다")
		void joiningBySpace_withNullStrings_excludesNull() {
			// when
			String result = StringUtil.joiningBySpace("Hello", null, "World");

			// then
			assertThat(result).isEqualTo("Hello World");
		}

		@Test
		@DisplayName("모든 값이 빈 문자열이면 빈 문자열을 반환한다")
		void joiningBySpace_allEmpty_returnsEmptyString() {
			// when
			String result = StringUtil.joiningBySpace("", "", "");

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("joining 테스트")
	class JoiningTest {

		@Test
		@DisplayName("커스텀 구분자로 문자열을 결합한다")
		void joining_customDelimiter_returnsDelimitedString() {
			// when
			String result = StringUtil.joining(",", "a", "b", "c");

			// then
			assertThat(result).isEqualTo("a,b,c");
		}

		@Test
		@DisplayName("하이픈 구분자로 문자열을 결합한다")
		void joining_hyphenDelimiter_returnsHyphenSeparated() {
			// when
			String result = StringUtil.joining("-", "2024", "01", "15");

			// then
			assertThat(result).isEqualTo("2024-01-15");
		}

		@Test
		@DisplayName("빈 문자열은 결과에서 제외된다")
		void joining_withEmptyStrings_excludesEmpty() {
			// when
			String result = StringUtil.joining("|", "first", "", "third");

			// then
			assertThat(result).isEqualTo("first|third");
		}

		@Test
		@DisplayName("null 값은 결과에서 제외된다")
		void joining_withNullStrings_excludesNull() {
			// when
			String result = StringUtil.joining(";", "first", null, "third");

			// then
			assertThat(result).isEqualTo("first;third");
		}

		@Test
		@DisplayName("빈 구분자로 문자열을 결합한다")
		void joining_emptyDelimiter_returnsConcatenated() {
			// when
			String result = StringUtil.joining("", "a", "b", "c");

			// then
			assertThat(result).isEqualTo("abc");
		}

		@Test
		@DisplayName("단일 문자열은 그대로 반환한다")
		void joining_singleString_returnsSameString() {
			// when
			String result = StringUtil.joining(",", "only");

			// then
			assertThat(result).isEqualTo("only");
		}

		@Test
		@DisplayName("모든 값이 비어있으면 빈 문자열을 반환한다")
		void joining_allEmpty_returnsEmptyString() {
			// when
			String result = StringUtil.joining(",", "", null, "");

			// then
			assertThat(result).isEmpty();
		}
	}
}
