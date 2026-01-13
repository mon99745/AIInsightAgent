package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.enums.ConfidenceLevel;
import com.aiinsightagent.app.repository.PreparedContextRepository;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PreparedContextService 테스트")
class PreparedContextServiceTest {

	@Mock
	private PreparedContextRepository contextRepository;

	@InjectMocks
	private PreparedContextService preparedContextService;

	private Actor actor;
	private Context context;
	private PreparedContext preparedContext;
	private Map<String, String> contextData;

	@BeforeEach
	void setUp() {
		// Actor 생성
		actor = Actor.create("test-user-key");
		ReflectionTestUtils.setField(actor, "actorId", 1L);

		// Context 데이터 생성
		contextData = new HashMap<>();
		contextData.put("averagePace", "6:00");
		contextData.put("totalDistance", "100km");
		contextData.put("runningDays", "30");

		// Context 생성
		context = Context.builder()
				.category("running_history")
				.data(contextData)
				.build();

		// PreparedContext 생성
		preparedContext = new PreparedContext(actor, "running_history", contextData.toString());
		ReflectionTestUtils.setField(preparedContext, "contextId", 1L);
		ReflectionTestUtils.setField(preparedContext, "regDate", LocalDateTime.now());
	}

	@Test
	@DisplayName("findByActorKey - Actor에 대한 PreparedContext 조회 성공")
	void findByActorKey_Success() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(preparedContext));

		// when
		Optional<PreparedContext> result = preparedContextService.findByActorKey(actor);

		// then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(preparedContext);
		assertThat(result.get().getActor()).isEqualTo(actor);
		assertThat(result.get().getContextType()).isEqualTo("running_history");

		verify(contextRepository, times(1)).findByActor(actor);
	}

	@Test
	@DisplayName("findByActorKey - Actor에 대한 PreparedContext가 없는 경우 Empty 반환")
	void findByActorKey_NotFound_ReturnsEmpty() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());

		// when
		Optional<PreparedContext> result = preparedContextService.findByActorKey(actor);

		// then
		assertThat(result).isEmpty();

		verify(contextRepository, times(1)).findByActor(actor);
	}

	@Test
	@DisplayName("save - 새로운 PreparedContext 저장 성공")
	void save_NewContext_Success() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(preparedContext);

		// when
		PreparedContext result = preparedContextService.save(actor, context);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getActor()).isEqualTo(actor);
		assertThat(result.getContextType()).isEqualTo("running_history");

		verify(contextRepository, times(1)).findByActor(actor);
		verify(contextRepository, times(1)).save(any(PreparedContext.class));
	}

	@Test
	@DisplayName("save - ArgumentCaptor로 저장되는 PreparedContext 검증")
	void save_VerifyPreparedContextWithArgumentCaptor() {
		// given
		ArgumentCaptor<PreparedContext> captor = ArgumentCaptor.forClass(PreparedContext.class);

		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());
		given(contextRepository.save(captor.capture()))
				.willReturn(preparedContext);

		// when
		preparedContextService.save(actor, context);

		// then
		PreparedContext capturedContext = captor.getValue();
		assertThat(capturedContext.getActor()).isEqualTo(actor);
		assertThat(capturedContext.getContextType()).isEqualTo("running_history");
		assertThat(capturedContext.getContextPayload()).isEqualTo(contextData.toString());
		assertThat(capturedContext.getContextScope()).isEqualTo("ACTOR");
		assertThat(capturedContext.isActive()).isTrue();
		assertThat(capturedContext.getConfidenceLevel()).isEqualTo(ConfidenceLevel.MEDIUM);
	}

	@Test
	@DisplayName("save - 이미 존재하는 Actor의 Context 저장 시 예외 발생")
	void save_ExistingContext_ThrowsException() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(preparedContext));

		// when & then
		assertThatThrownBy(() -> preparedContextService.save(actor, context))
				.isInstanceOf(InsightException.class)
				.hasMessageContaining(InsightError.EXIST_ACTOR_PREPARED_CONTEXT.toString())
				.hasMessageContaining(actor.getActorKey());

		verify(contextRepository, times(1)).findByActor(actor);
		verify(contextRepository, never()).save(any(PreparedContext.class));
	}

	@Test
	@DisplayName("save - 다양한 카테고리의 Context 저장")
	void save_DifferentCategories_Success() {
		// given
		Context healthContext = Context.builder()
				.category("health_metrics")
				.data(Map.of("heartRate", "170", "calories", "500"))
				.build();

		PreparedContext healthPreparedContext = new PreparedContext(
				actor, "health_metrics", healthContext.getData().toString()
		);

		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(healthPreparedContext);

		// when
		PreparedContext result = preparedContextService.save(actor, healthContext);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getContextType()).isEqualTo("health_metrics");

		verify(contextRepository, times(1)).save(any(PreparedContext.class));
	}

	@Test
	@DisplayName("get - Actor에 대한 PreparedContext 조회 성공")
	void get_Success() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(preparedContext));

		// when
		PreparedContext result = preparedContextService.get(actor);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(preparedContext);
		assertThat(result.getActor()).isEqualTo(actor);

		verify(contextRepository, times(1)).findByActor(actor);
	}

	@Test
	@DisplayName("get - Actor에 대한 PreparedContext가 없는 경우 예외 발생")
	void get_NotFound_ThrowsException() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> preparedContextService.get(actor))
				.isInstanceOf(InsightException.class)
				.hasMessageContaining(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT.toString())
				.hasMessageContaining(actor.getActorKey());

		verify(contextRepository, times(1)).findByActor(actor);
	}

	@Test
	@DisplayName("update - 기존 PreparedContext 업데이트 성공")
	void update_Success() {
		// given
		Context updatedContext = Context.builder()
				.category("updated_category")
				.data(Map.of("newField", "newValue"))
				.build();

		PreparedContext existingContext = new PreparedContext(
				actor, "old_category", "old_data"
		);
		ReflectionTestUtils.setField(existingContext, "contextId", 1L);

		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(existingContext));
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(existingContext);

		// when
		PreparedContext result = preparedContextService.update(actor, updatedContext);

		// then
		assertThat(result).isNotNull();

		verify(contextRepository, times(1)).findByActor(actor);
		verify(contextRepository, times(1)).save(existingContext);
	}

	@Test
	@DisplayName("update - PreparedContext가 없는 경우 예외 발생")
	void update_NotFound_ThrowsException() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> preparedContextService.update(actor, context))
				.isInstanceOf(InsightException.class)
				.hasMessageContaining(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT.toString())
				.hasMessageContaining(actor.getActorKey());

		verify(contextRepository, times(1)).findByActor(actor);
		verify(contextRepository, never()).save(any(PreparedContext.class));
	}

	@Test
	@DisplayName("update - update 메서드 호출 검증")
	void update_CallsUpdateMethod() {
		// given
		Context updatedContext = Context.builder()
				.category("new_category")
				.data(Map.of("key", "value"))
				.build();

		PreparedContext spyContext = spy(new PreparedContext(
				actor, "old_category", "old_data"
		));
		ReflectionTestUtils.setField(spyContext, "contextId", 1L);

		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(spyContext));
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(spyContext);

		// when
		preparedContextService.update(actor, updatedContext);

		// then
		verify(spyContext, times(1)).update("new_category", updatedContext.getData().toString());
		verify(contextRepository, times(1)).save(spyContext);
	}

	@Test
	@DisplayName("delete - PreparedContext 삭제 성공")
	void delete_Success() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(preparedContext));
		doNothing().when(contextRepository).deleteById(1L);

		// when
		preparedContextService.delete(actor);

		// then
		verify(contextRepository, times(1)).findByActor(actor);
		verify(contextRepository, times(1)).deleteById(1L);
	}

	@Test
	@DisplayName("delete - PreparedContext가 없는 경우 예외 발생")
	void delete_NotFound_ThrowsException() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> preparedContextService.delete(actor))
				.isInstanceOf(InsightException.class)
				.hasMessageContaining(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT.toString())
				.hasMessageContaining(actor.getActorKey());

		verify(contextRepository, times(1)).findByActor(actor);
		verify(contextRepository, never()).deleteById(any());
	}

	@Test
	@DisplayName("delete - contextId로 삭제 확인")
	void delete_DeletesByContextId() {
		// given
		PreparedContext contextToDelete = new PreparedContext(
				actor, "category", "data"
		);
		Long contextId = 99L;
		ReflectionTestUtils.setField(contextToDelete, "contextId", contextId);

		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(contextToDelete));
		doNothing().when(contextRepository).deleteById(contextId);

		// when
		preparedContextService.delete(actor);

		// then
		verify(contextRepository, times(1)).deleteById(contextId);
	}

	@Test
	@DisplayName("save - Context 데이터가 빈 Map인 경우")
	void save_EmptyContextData_Success() {
		// given
		Context emptyContext = Context.builder()
				.category("empty_category")
				.data(new HashMap<>())
				.build();

		PreparedContext emptyPreparedContext = new PreparedContext(
				actor, "empty_category", "{}"
		);

		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty());
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(emptyPreparedContext);

		// when
		PreparedContext result = preparedContextService.save(actor, emptyContext);

		// then
		assertThat(result).isNotNull();
		verify(contextRepository, times(1)).save(any(PreparedContext.class));
	}

	@Test
	@DisplayName("save - 여러 Actor에 대한 Context 저장")
	void save_MultipleActors_Success() {
		// given
		Actor actor1 = Actor.create("user-1");
		Actor actor2 = Actor.create("user-2");

		PreparedContext context1 = new PreparedContext(actor1, "category1", "data1");
		PreparedContext context2 = new PreparedContext(actor2, "category2", "data2");

		given(contextRepository.findByActor(actor1))
				.willReturn(Optional.empty());
		given(contextRepository.findByActor(actor2))
				.willReturn(Optional.empty());
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(context1)
				.willReturn(context2);

		// when
		PreparedContext result1 = preparedContextService.save(actor1, context);
		PreparedContext result2 = preparedContextService.save(actor2, context);

		// then
		assertThat(result1).isNotNull();
		assertThat(result2).isNotNull();
		verify(contextRepository, times(2)).save(any(PreparedContext.class));
	}

	@Test
	@DisplayName("asPromptText - PreparedContext를 프롬프트 텍스트로 변환")
	void asPromptText_ReturnsContextPayload() {
		// given
		String expectedPayload = contextData.toString();
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(preparedContext));

		// when
		Optional<PreparedContext> result = preparedContextService.findByActorKey(actor);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().asPromptText()).isEqualTo(expectedPayload);
	}

	@Test
	@DisplayName("update - 카테고리와 데이터 모두 변경")
	void update_ChangeCategoryAndData_Success() {
		// given
		Map<String, String> newData = new HashMap<>();
		newData.put("pace", "5:30");
		newData.put("distance", "15km");

		Context newContext = Context.builder()
				.category("new_running_stats")
				.data(newData)
				.build();

		PreparedContext existingContext = new PreparedContext(
				actor, "old_category", "old_data"
		);
		ReflectionTestUtils.setField(existingContext, "contextId", 1L);

		given(contextRepository.findByActor(actor))
				.willReturn(Optional.of(existingContext));
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(existingContext);

		// when
		preparedContextService.update(actor, newContext);

		// then
		verify(contextRepository, times(1)).findByActor(actor);
		verify(contextRepository, times(1)).save(existingContext);
	}

	@Test
	@DisplayName("save와 get - 저장 후 조회 흐름")
	void saveAndGet_Flow() {
		// given
		given(contextRepository.findByActor(actor))
				.willReturn(Optional.empty())
				.willReturn(Optional.of(preparedContext));
		given(contextRepository.save(any(PreparedContext.class)))
				.willReturn(preparedContext);

		// when
		PreparedContext saved = preparedContextService.save(actor, context);
		PreparedContext retrieved = preparedContextService.get(actor);

		// then
		assertThat(saved).isNotNull();
		assertThat(retrieved).isNotNull();
		assertThat(saved).isEqualTo(retrieved);

		verify(contextRepository, times(2)).findByActor(actor);
		verify(contextRepository, times(1)).save(any(PreparedContext.class));
	}
}