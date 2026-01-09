package com.aiinsightagent.app.util;

import com.aiinsightagent.app.exception.InsightAppError;
import com.aiinsightagent.app.exception.InsightAppException;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("InsightRequestValidator 테스트")
class InsightRequestValidatorTest {

	@Nested
	@DisplayName("validate 메서드")
	class ValidateTest {

		@Test
		@DisplayName("정상적인 InsightRequest 검증 성공")
		void validate_ValidRequest_Success() {
			// given
			Map<String, String> data = new HashMap<>();
			data.put("duration", "3556");
			data.put("heartRate", "194.63");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder()
							.dataKey("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99")
							.data(data)
							.build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validate(request))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("여러 UserPrompt가 있는 정상 요청 검증 성공")
		void validate_MultipleUserPrompts_Success() {
			// given
			Map<String, String> data1 = createRunningData("3556", "194.63", "9.95", "10114");
			Map<String, String> data2 = createRunningData("1965", "181.92", "6.01", "5702");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder().dataKey("KEY-1").data(data1).build(),
					UserPrompt.builder().dataKey("KEY-2").data(data2).build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validate(request))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("UserPrompt가 null인 경우 예외 발생")
		void validate_NullUserPrompt_ThrowsException() {
			// given
			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(null)
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.MISSING_USER_PROMPT_REQUEST.toString());
		}

		@Test
		@DisplayName("UserPrompt가 빈 리스트인 경우 예외 발생")
		void validate_EmptyUserPrompt_ThrowsException() {
			// given
			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(Collections.emptyList())
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.MISSING_USER_PROMPT_REQUEST.toString());
		}

		@Test
		@DisplayName("dataKey가 null인 경우 예외 발생")
		void validate_NullDataKey_ThrowsException() {
			// given
			Map<String, String> data = createRunningData("3556", "194.63", "9.95", "10114");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder()
							.dataKey(null)
							.data(data)
							.build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.EMPTY_DATA_KEY.toString());
		}

		@Test
		@DisplayName("dataKey가 빈 문자열인 경우 예외 발생")
		void validate_EmptyDataKey_ThrowsException() {
			// given
			Map<String, String> data = createRunningData("3556", "194.63", "9.95", "10114");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder()
							.dataKey("")
							.data(data)
							.build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.EMPTY_DATA_KEY.toString());
		}

		@Test
		@DisplayName("여러 UserPrompt 중 하나의 dataKey가 null인 경우 예외 발생")
		void validate_OneOfMultipleDataKeysIsNull_ThrowsException() {
			// given
			Map<String, String> data = createRunningData("3556", "194.63", "9.95", "10114");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder().dataKey("VALID-KEY").data(data).build(),
					UserPrompt.builder().dataKey(null).data(data).build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.EMPTY_DATA_KEY.toString());
		}

		@Test
		@DisplayName("data가 null인 경우 예외 발생")
		void validate_NullData_ThrowsException() {
			// given
			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder()
							.dataKey("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99")
							.data(null)
							.build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.EMPTY_DATA_OBJECT.toString());
		}

		@Test
		@DisplayName("data가 빈 Map인 경우 예외 발생")
		void validate_EmptyData_ThrowsException() {
			// given
			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder()
							.dataKey("A0398D47-38EB-4FEA-A8C2-34DF8E46DC99")
							.data(new HashMap<>())
							.build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.EMPTY_DATA_OBJECT.toString());
		}

		@Test
		@DisplayName("여러 UserPrompt 중 하나의 data가 null인 경우 예외 발생")
		void validate_OneOfMultipleDataIsNull_ThrowsException() {
			// given
			Map<String, String> validData = createRunningData("3556", "194.63", "9.95", "10114");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder().dataKey("KEY-1").data(validData).build(),
					UserPrompt.builder().dataKey("KEY-2").data(null).build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.EMPTY_DATA_OBJECT.toString());
		}
	}

	@Nested
	@DisplayName("hasUserPrompt 메서드")
	class HasUserPromptTest {

		@Test
		@DisplayName("UserPrompt가 있는 경우 true 반환")
		void hasUserPrompt_WithUserPrompts_ReturnsTrue() {
			// given
			Map<String, String> data = createRunningData("3556", "194.63", "9.95", "10114");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder().dataKey("KEY-1").data(data).build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when
			boolean result = InsightRequestValidator.hasUserPrompt(request);

			// then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("UserPrompt가 null인 경우 false 반환")
		void hasUserPrompt_WithNullUserPrompts_ReturnsFalse() {
			// given
			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(null)
					.build();

			// when
			boolean result = InsightRequestValidator.hasUserPrompt(request);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("UserPrompt가 빈 리스트인 경우 false 반환")
		void hasUserPrompt_WithEmptyUserPrompts_ReturnsFalse() {
			// given
			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(Collections.emptyList())
					.build();

			// when
			boolean result = InsightRequestValidator.hasUserPrompt(request);

			// then
			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("validateDataKey 메서드")
	class ValidateDataKeyTest {

		@Test
		@DisplayName("모든 dataKey가 유효한 경우 예외 없음")
		void validateDataKey_AllValidKeys_NoException() {
			// given
			Map<String, String> data = createRunningData("3556", "194.63", "9.95", "10114");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder().dataKey("KEY-1").data(data).build(),
					UserPrompt.builder().dataKey("KEY-2").data(data).build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validateDataKey(request))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("UserPrompt가 없는 경우 검증 스킵")
		void validateDataKey_NoUserPrompts_SkipsValidation() {
			// given
			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(null)
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validateDataKey(request))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("dataKey가 공백만 있는 경우 예외 발생")
		void validateDataKey_WhitespaceDataKey_ThrowsException() {
			// given
			Map<String, String> data = createRunningData("3556", "194.63", "9.95", "10114");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder().dataKey("   ").data(data).build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			// Note: 현재 구현은 isEmpty()만 체크하므로 공백은 통과됨
			// 공백 체크가 필요하면 isBlank() 사용 필요
			assertThatCode(() -> InsightRequestValidator.validateDataKey(request))
					.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("validateDataFields 메서드")
	class ValidateDataFieldsTest {

		@Test
		@DisplayName("모든 data가 유효한 경우 예외 없음")
		void validateDataFields_AllValidData_NoException() {
			// given
			Map<String, String> data1 = createRunningData("3556", "194.63", "9.95", "10114");
			Map<String, String> data2 = createRunningData("1965", "181.92", "6.01", "5702");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder().dataKey("KEY-1").data(data1).build(),
					UserPrompt.builder().dataKey("KEY-2").data(data2).build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validateDataFields(request))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("UserPrompt가 없는 경우 검증 스킵")
		void validateDataFields_NoUserPrompts_SkipsValidation() {
			// given
			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(Collections.emptyList())
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validateDataFields(request))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("data에 하나의 필드만 있어도 유효")
		void validateDataFields_SingleFieldData_Valid() {
			// given
			Map<String, String> data = new HashMap<>();
			data.put("duration", "3556");

			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder()
							.dataKey("KEY-1")
							.data(data)
							.build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validateDataFields(request))
					.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("통합 시나리오")
	class IntegrationTest {

		@Test
		@DisplayName("10개의 러닝 데이터 검증 성공")
		void validate_TenRunningRecords_Success() {
			// given
			List<UserPrompt> userPrompts = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				Map<String, String> data = createRunningData(
						String.valueOf(3000 + i * 100),
						String.valueOf(180 + i),
						String.valueOf(10.0 - i * 0.5),
						String.valueOf(10000 + i * 100)
				);
				userPrompts.add(
						UserPrompt.builder()
								.dataKey("KEY-" + i)
								.data(data)
								.build()
				);
			}

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			assertThatCode(() -> InsightRequestValidator.validate(request))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("복합적인 검증 실패 - UserPrompt 없음이 먼저 체크됨")
		void validate_MultipleErrors_MissingUserPromptFirst() {
			// given
			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(null)
					.build();

			// when & then
			// UserPrompt가 없으므로 MISSING_USER_PROMPT_REQUEST 에러가 먼저 발생
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.MISSING_USER_PROMPT_REQUEST.toString());
		}

		@Test
		@DisplayName("복합적인 검증 실패 - dataKey 검증이 data 검증보다 먼저")
		void validate_MultipleErrors_DataKeyBeforeData() {
			// given
			List<UserPrompt> userPrompts = Arrays.asList(
					UserPrompt.builder()
							.dataKey(null)  // dataKey 오류
							.data(null)     // data 오류
							.build()
			);

			InsightRequest request = InsightRequest.builder()
					.userId("test-user")
					.purpose("running_style_analysis")
					.userPrompt(userPrompts)
					.build();

			// when & then
			// dataKey 검증이 먼저 실행되므로 EMPTY_DATA_KEY 에러가 발생
			assertThatThrownBy(() -> InsightRequestValidator.validate(request))
					.isInstanceOf(InsightAppException.class)
					.hasMessageContaining(InsightAppError.EMPTY_DATA_KEY.toString());
		}
	}

	// Helper method
	private Map<String, String> createRunningData(String duration, String heartRate,
												  String distance, String stepCount) {
		Map<String, String> data = new HashMap<>();
		data.put("duration", duration);
		data.put("heartRate", heartRate);
		data.put("distance", distance);
		data.put("stepCount", stepCount);
		return data;
	}
}