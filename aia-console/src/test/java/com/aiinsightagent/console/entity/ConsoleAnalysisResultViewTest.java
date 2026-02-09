package com.aiinsightagent.console.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleAnalysisResultView 테스트")
class ConsoleAnalysisResultViewTest {

	@Test
	@DisplayName("getAnalysisTypeName - STYLE(0) 매핑")
	void getAnalysisTypeName_WithStyle_ReturnsStyle() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();
		setField(view, "analysisType", "0");

		String typeName = view.getAnalysisTypeName();

		assertThat(typeName).isEqualTo("STYLE");
	}

	@Test
	@DisplayName("getAnalysisTypeName - PATTERN(1) 매핑")
	void getAnalysisTypeName_WithPattern_ReturnsPattern() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();
		setField(view, "analysisType", "1");

		String typeName = view.getAnalysisTypeName();

		assertThat(typeName).isEqualTo("PATTERN");
	}

	@Test
	@DisplayName("getAnalysisTypeName - CLASSIFICATION(2) 매핑")
	void getAnalysisTypeName_WithClassification_ReturnsClassification() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();
		setField(view, "analysisType", "2");

		String typeName = view.getAnalysisTypeName();

		assertThat(typeName).isEqualTo("CLASSIFICATION");
	}

	@Test
	@DisplayName("getAnalysisTypeName - null인 경우 대시 반환")
	void getAnalysisTypeName_WithNull_ReturnsDash() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();
		setField(view, "analysisType", null);

		String typeName = view.getAnalysisTypeName();

		assertThat(typeName).isEqualTo("-");
	}

	@Test
	@DisplayName("getAnalysisTypeName - 매핑되지 않은 값은 원본 반환")
	void getAnalysisTypeName_WithUnmappedValue_ReturnsOriginal() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();
		setField(view, "analysisType", "3");

		String typeName = view.getAnalysisTypeName();

		assertThat(typeName).isEqualTo("3");
	}

	@Test
	@DisplayName("getAnalysisTypeName - 문자열 타입은 원본 반환")
	void getAnalysisTypeName_WithStringType_ReturnsOriginal() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();
		setField(view, "analysisType", "CUSTOM_TYPE");

		String typeName = view.getAnalysisTypeName();

		assertThat(typeName).isEqualTo("CUSTOM_TYPE");
	}

	@Test
	@DisplayName("엔티티 필드 getter 검증")
	void entityFields_GettersWork() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();
		setField(view, "resultId", 1L);
		setField(view, "requestId", UUID.randomUUID());
		setField(view, "actorId", 100L);
		setField(view, "inputId", 200L);
		setField(view, "analysisType", "0");
		setField(view, "analysisVersion", "v1.0");
		setField(view, "resultPayload", "{}");
		setField(view, "status", "SUCCESS");
		setField(view, "regDate", LocalDateTime.now());

		assertThat(view.getResultId()).isEqualTo(1L);
		assertThat(view.getRequestId()).isNotNull();
		assertThat(view.getActorId()).isEqualTo(100L);
		assertThat(view.getInputId()).isEqualTo(200L);
		assertThat(view.getAnalysisType()).isEqualTo("0");
		assertThat(view.getAnalysisVersion()).isEqualTo("v1.0");
		assertThat(view.getResultPayload()).isEqualTo("{}");
		assertThat(view.getStatus()).isEqualTo("SUCCESS");
		assertThat(view.getRegDate()).isNotNull();
	}

	@Test
	@DisplayName("ORDINAL_TO_TYPE 맵이 정확히 3개 엔트리 보유")
	void ordinalToTypeMap_HasThreeEntries() throws Exception {
		ConsoleAnalysisResultView view = new ConsoleAnalysisResultView();

		// 0, 1, 2 모두 매핑되어야 함
		setField(view, "analysisType", "0");
		assertThat(view.getAnalysisTypeName()).isEqualTo("STYLE");

		setField(view, "analysisType", "1");
		assertThat(view.getAnalysisTypeName()).isEqualTo("PATTERN");

		setField(view, "analysisType", "2");
		assertThat(view.getAnalysisTypeName()).isEqualTo("CLASSIFICATION");
	}

	private void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}