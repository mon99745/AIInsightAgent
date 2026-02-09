package com.aiinsightagent.app.entity;

import com.aiinsightagent.app.enums.ActorStatus;
import com.aiinsightagent.app.enums.ActorType;
import com.aiinsightagent.app.enums.AnalysisStatus;
import com.aiinsightagent.app.enums.AnalysisType;
import com.aiinsightagent.app.enums.InputType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnalysisResult 테스트")
class AnalysisResultTest {

	@Test
	@DisplayName("생성자 - 정상 생성 및 필드 검증")
	void constructor_CreatesInstanceWithAllFields() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult result = new AnalysisResult(
				actor,
				rawData,
				AnalysisType.STYLE,
				AnalysisStatus.SUCCESS,
				"{\"result\":\"data\"}",
				"v1.0"
		);

		assertThat(result.getActor()).isEqualTo(actor);
		assertThat(result.getAnalysisInput()).isEqualTo(rawData);
		assertThat(result.getAnalysisType()).isEqualTo(AnalysisType.STYLE);
		assertThat(result.getStatus()).isEqualTo(AnalysisStatus.SUCCESS);
		assertThat(result.getResultPayload()).isEqualTo("{\"result\":\"data\"}");
		assertThat(result.getAnalysisVersion()).isEqualTo("v1.0");
		assertThat(result.getRequestId()).isNotNull();
		assertThat(result.getRegDate()).isNotNull();
	}

	@Test
	@DisplayName("생성자 - requestId는 자동 생성되며 UUID 형식")
	void constructor_GeneratesRequestId() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult result = new AnalysisResult(
				actor,
				rawData,
				AnalysisType.PATTERN,
				AnalysisStatus.SUCCESS,
				"{}",
				"v1.0"
		);

		assertThat(result.getRequestId()).isNotNull();
		assertThat(result.getRequestId().toString()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
	}

	@Test
	@DisplayName("생성자 - regDate는 현재 시간으로 자동 설정")
	void constructor_SetsRegDateToNow() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult result = new AnalysisResult(
				actor,
				rawData,
				AnalysisType.CLASSIFICATION,
				AnalysisStatus.FAILED,
				"{}",
				"v1.0"
		);

		assertThat(result.getRegDate()).isNotNull();
		assertThat(result.getRegDate()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
	}

	@Test
	@DisplayName("생성자 - 다양한 AnalysisType 처리")
	void constructor_WithVariousAnalysisTypes() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult styleResult = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "{}", "v1.0"
		);
		assertThat(styleResult.getAnalysisType()).isEqualTo(AnalysisType.STYLE);

		AnalysisResult patternResult = new AnalysisResult(
				actor, rawData, AnalysisType.PATTERN, AnalysisStatus.SUCCESS, "{}", "v1.0"
		);
		assertThat(patternResult.getAnalysisType()).isEqualTo(AnalysisType.PATTERN);

		AnalysisResult classificationResult = new AnalysisResult(
				actor, rawData, AnalysisType.CLASSIFICATION, AnalysisStatus.SUCCESS, "{}", "v1.0"
		);
		assertThat(classificationResult.getAnalysisType()).isEqualTo(AnalysisType.CLASSIFICATION);
	}

	@Test
	@DisplayName("생성자 - 다양한 AnalysisStatus 처리")
	void constructor_WithVariousAnalysisStatuses() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult successResult = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "{}", "v1.0"
		);
		assertThat(successResult.getStatus()).isEqualTo(AnalysisStatus.SUCCESS);

		AnalysisResult failedResult = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.FAILED, "{}", "v1.0"
		);
		assertThat(failedResult.getStatus()).isEqualTo(AnalysisStatus.FAILED);
	}

	@Test
	@DisplayName("생성자 - 각 인스턴스는 고유한 requestId 보유")
	void constructor_EachInstanceHasUniqueRequestId() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult result1 = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "{}", "v1.0"
		);

		AnalysisResult result2 = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "{}", "v1.0"
		);

		assertThat(result1.getRequestId()).isNotEqualTo(result2.getRequestId());
	}

	@Test
	@DisplayName("생성자 - 빈 resultPayload 처리")
	void constructor_WithEmptyResultPayload() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult result = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "", "v1.0"
		);

		assertThat(result.getResultPayload()).isEmpty();
	}

	@Test
	@DisplayName("생성자 - 긴 resultPayload 처리")
	void constructor_WithLongResultPayload() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");
		String longPayload = "{\"data\":\"" + "x".repeat(10000) + "\"}";

		AnalysisResult result = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, longPayload, "v1.0"
		);

		assertThat(result.getResultPayload()).isEqualTo(longPayload);
		assertThat(result.getResultPayload().length()).isGreaterThan(10000);
	}

	@Test
	@DisplayName("생성자 - 다양한 analysisVersion 형식 처리")
	void constructor_WithVariousVersionFormats() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult result1 = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "{}", "1.0.0"
		);
		assertThat(result1.getAnalysisVersion()).isEqualTo("1.0.0");

		AnalysisResult result2 = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "{}", "v2.1"
		);
		assertThat(result2.getAnalysisVersion()).isEqualTo("v2.1");

		AnalysisResult result3 = new AnalysisResult(
				actor, rawData, AnalysisType.STYLE, AnalysisStatus.SUCCESS, "{}", "latest"
		);
		assertThat(result3.getAnalysisVersion()).isEqualTo("latest");
	}

	@Test
	@DisplayName("Getter 메서드 - 모든 필드 접근 가능")
	void getters_AllFieldsAccessible() {
		Actor actor = new Actor("test-actor", ActorType.DEVICE, ActorStatus.ACTIVE);
		AnalysisRawData rawData = new AnalysisRawData(actor, InputType.SOURCE_CODE, "test", "{}");

		AnalysisResult result = new AnalysisResult(
				actor, rawData, AnalysisType.PATTERN, AnalysisStatus.SUCCESS, "{}", "v1.0"
		);

		assertThat(result.getResultId()).isNull(); // ID는 persistence 전에는 null
		assertThat(result.getRequestId()).isNotNull();
		assertThat(result.getActor()).isNotNull();
		assertThat(result.getAnalysisInput()).isNotNull();
		assertThat(result.getAnalysisType()).isNotNull();
		assertThat(result.getAnalysisVersion()).isNotNull();
		assertThat(result.getResultPayload()).isNotNull();
		assertThat(result.getStatus()).isNotNull();
		assertThat(result.getRegDate()).isNotNull();
	}
}