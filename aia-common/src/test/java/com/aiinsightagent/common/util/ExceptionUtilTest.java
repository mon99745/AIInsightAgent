package com.aiinsightagent.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionUtilTest {

	@Nested
	@DisplayName("getNotRootMessage 테스트")
	class GetNotRootMessageTest {

		@Test
		@DisplayName("원인이 없는 예외의 경우 빈 문자열을 반환한다")
		void getNotRootMessage_noCause_returnsEmptyString() {
			// given
			Exception ex = new RuntimeException("Root error");

			// when
			String result = ExceptionUtil.getNotRootMessage(ex);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("원인이 있는 예외의 경우 메시지를 반환한다")
		void getNotRootMessage_withCause_returnsMessage() {
			// given
			Exception cause = new RuntimeException("Root cause");
			Exception ex = new RuntimeException("Wrapper error", cause);

			// when
			String result = ExceptionUtil.getNotRootMessage(ex);

			// then
			assertThat(result).isEqualTo("Wrapper error");
		}

		@Test
		@DisplayName("중첩된 원인이 있는 예외의 경우 메시지를 반환한다")
		void getNotRootMessage_nestedCause_returnsMessage() {
			// given
			Exception rootCause = new RuntimeException("Root cause");
			Exception midCause = new RuntimeException("Middle cause", rootCause);
			Exception ex = new RuntimeException("Top error", midCause);

			// when
			String result = ExceptionUtil.getNotRootMessage(ex);

			// then
			assertThat(result).isEqualTo("Top error");
		}

		@Test
		@DisplayName("자기 자신을 원인으로 가지는 예외의 경우 빈 문자열을 반환한다")
		void getNotRootMessage_selfCause_returnsEmptyString() {
			// given
			Exception ex = new RuntimeException("Self referencing");

			// when
			String result = ExceptionUtil.getNotRootMessage(ex);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("getRootCause 테스트")
	class GetRootCauseTest {

		@Test
		@DisplayName("원인이 없는 예외의 경우 자기 자신을 반환한다")
		void getRootCause_noCause_returnsSelf() {
			// given
			Exception ex = new RuntimeException("Root error");

			// when
			Throwable result = ExceptionUtil.getRootCause(ex);

			// then
			assertThat(result).isSameAs(ex);
		}

		@Test
		@DisplayName("단일 원인이 있는 예외의 경우 원인을 반환한다")
		void getRootCause_singleCause_returnsCause() {
			// given
			Exception cause = new RuntimeException("Root cause");
			Exception ex = new RuntimeException("Wrapper error", cause);

			// when
			Throwable result = ExceptionUtil.getRootCause(ex);

			// then
			assertThat(result).isSameAs(cause);
		}

		@Test
		@DisplayName("중첩된 원인이 있는 예외의 경우 가장 깊은 원인을 반환한다")
		void getRootCause_nestedCause_returnsDeepestCause() {
			// given
			Exception rootCause = new RuntimeException("Root cause");
			Exception midCause = new RuntimeException("Middle cause", rootCause);
			Exception ex = new RuntimeException("Top error", midCause);

			// when
			Throwable result = ExceptionUtil.getRootCause(ex);

			// then
			assertThat(result).isSameAs(rootCause);
		}

		@Test
		@DisplayName("3단계 중첩된 원인의 경우 가장 깊은 원인을 반환한다")
		void getRootCause_threeLevelNested_returnsDeepestCause() {
			// given
			Exception level0 = new RuntimeException("Level 0");
			Exception level1 = new RuntimeException("Level 1", level0);
			Exception level2 = new RuntimeException("Level 2", level1);
			Exception level3 = new RuntimeException("Level 3", level2);

			// when
			Throwable result = ExceptionUtil.getRootCause(level3);

			// then
			assertThat(result).isSameAs(level0);
			assertThat(result.getMessage()).isEqualTo("Level 0");
		}

		@Test
		@DisplayName("다른 타입의 예외 체인에서도 루트 원인을 반환한다")
		void getRootCause_mixedExceptionTypes_returnsRootCause() {
			// given
			IllegalArgumentException rootCause = new IllegalArgumentException("Invalid argument");
			IllegalStateException midCause = new IllegalStateException("Invalid state", rootCause);
			RuntimeException ex = new RuntimeException("Runtime error", midCause);

			// when
			Throwable result = ExceptionUtil.getRootCause(ex);

			// then
			assertThat(result).isSameAs(rootCause);
			assertThat(result).isInstanceOf(IllegalArgumentException.class);
		}
	}
}
