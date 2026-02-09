package com.aiinsightagent.console.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsolePreparedContextView 테스트")
class ConsolePreparedContextViewTest {

	@Test
	@DisplayName("엔티티 필드 getter 검증")
	void entityFields_GettersWork() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();
		LocalDateTime now = LocalDateTime.now();

		setField(view, "contextId", 1L);
		setField(view, "actorId", 100L);
		setField(view, "contextType", "USER_PREFERENCE");
		setField(view, "contextScope", "GLOBAL");
		setField(view, "contextPayload", "{\"key\":\"value\"}");
		setField(view, "confidenceLevel", "HIGH");
		setField(view, "active", true);
		setField(view, "regDate", now);
		setField(view, "modDate", now);

		assertThat(view.getContextId()).isEqualTo(1L);
		assertThat(view.getActorId()).isEqualTo(100L);
		assertThat(view.getContextType()).isEqualTo("USER_PREFERENCE");
		assertThat(view.getContextScope()).isEqualTo("GLOBAL");
		assertThat(view.getContextPayload()).isEqualTo("{\"key\":\"value\"}");
		assertThat(view.getConfidenceLevel()).isEqualTo("HIGH");
		assertThat(view.isActive()).isTrue();
		assertThat(view.getRegDate()).isEqualTo(now);
		assertThat(view.getModDate()).isEqualTo(now);
	}

	@Test
	@DisplayName("active 필드 - false 값 처리")
	void activeField_WithFalseValue() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();
		setField(view, "active", false);

		assertThat(view.isActive()).isFalse();
	}

	@Test
	@DisplayName("contextPayload - null 값 처리")
	void contextPayload_WithNullValue() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();
		setField(view, "contextPayload", null);

		assertThat(view.getContextPayload()).isNull();
	}

	@Test
	@DisplayName("modDate - null 값 처리 (수정되지 않은 경우)")
	void modDate_WithNullValue() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();
		LocalDateTime regDate = LocalDateTime.now();
		setField(view, "regDate", regDate);
		setField(view, "modDate", null);

		assertThat(view.getRegDate()).isEqualTo(regDate);
		assertThat(view.getModDate()).isNull();
	}

	@Test
	@DisplayName("모든 필드가 null인 경우")
	void allFields_WithNullValues() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();

		assertThat(view.getContextId()).isNull();
		assertThat(view.getActorId()).isNull();
		assertThat(view.getContextType()).isNull();
		assertThat(view.getContextScope()).isNull();
		assertThat(view.getContextPayload()).isNull();
		assertThat(view.getConfidenceLevel()).isNull();
		assertThat(view.isActive()).isFalse(); // boolean 기본값
		assertThat(view.getRegDate()).isNull();
		assertThat(view.getModDate()).isNull();
	}

	@Test
	@DisplayName("다양한 contextType 값 처리")
	void contextType_WithVariousValues() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();

		setField(view, "contextType", "SYSTEM");
		assertThat(view.getContextType()).isEqualTo("SYSTEM");

		setField(view, "contextType", "USER");
		assertThat(view.getContextType()).isEqualTo("USER");

		setField(view, "contextType", "ANALYSIS");
		assertThat(view.getContextType()).isEqualTo("ANALYSIS");
	}

	@Test
	@DisplayName("다양한 contextScope 값 처리")
	void contextScope_WithVariousValues() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();

		setField(view, "contextScope", "LOCAL");
		assertThat(view.getContextScope()).isEqualTo("LOCAL");

		setField(view, "contextScope", "SESSION");
		assertThat(view.getContextScope()).isEqualTo("SESSION");

		setField(view, "contextScope", "GLOBAL");
		assertThat(view.getContextScope()).isEqualTo("GLOBAL");
	}

	@Test
	@DisplayName("다양한 confidenceLevel 값 처리")
	void confidenceLevel_WithVariousValues() throws Exception {
		ConsolePreparedContextView view = new ConsolePreparedContextView();

		setField(view, "confidenceLevel", "LOW");
		assertThat(view.getConfidenceLevel()).isEqualTo("LOW");

		setField(view, "confidenceLevel", "MEDIUM");
		assertThat(view.getConfidenceLevel()).isEqualTo("MEDIUM");

		setField(view, "confidenceLevel", "HIGH");
		assertThat(view.getConfidenceLevel()).isEqualTo("HIGH");
	}

	private void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}