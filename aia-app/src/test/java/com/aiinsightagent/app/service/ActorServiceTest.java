package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.repository.ActorRepository;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActorService 테스트")
class ActorServiceTest {

	@Mock
	private ActorRepository actorRepository;

	@InjectMocks
	private ActorService actorService;

	private String actorKey;
	private Actor actor;

	@BeforeEach
	void setUp() {
		actorKey = "test-actor-key";
		actor = Actor.create(actorKey);
	}

	@Test
	@DisplayName("getOrCreate - 기존 Actor가 존재하는 경우 조회")
	void getOrCreate_WhenActorExists_ReturnsExistingActor() {
		// given
		given(actorRepository.findByActorKey(actorKey))
				.willReturn(Optional.of(actor));

		// when
		Actor result = actorService.getOrCreate(actorKey);

		// then
		assertThat(result).isEqualTo(actor);
		verify(actorRepository, times(1)).findByActorKey(actorKey);
		verify(actorRepository, never()).save(any(Actor.class));
	}

	@Test
	@DisplayName("getOrCreate - Actor가 존재하지 않는 경우 생성")
	void getOrCreate_WhenActorNotExists_CreatesNewActor() {
		// given
		given(actorRepository.findByActorKey(actorKey))
				.willReturn(Optional.empty());
		given(actorRepository.save(any(Actor.class)))
				.willReturn(actor);

		// when
		Actor result = actorService.getOrCreate(actorKey);

		// then
		assertThat(result).isNotNull();
		verify(actorRepository, times(1)).findByActorKey(actorKey);
		verify(actorRepository, times(1)).save(any(Actor.class));
	}

	@Test
	@DisplayName("get - Actor가 존재하는 경우 조회 성공")
	void get_WhenActorExists_ReturnsActor() {
		// given
		given(actorRepository.findByActorKey(actorKey))
				.willReturn(Optional.of(actor));

		// when
		Actor result = actorService.get(actorKey);

		// then
		assertThat(result).isEqualTo(actor);
		verify(actorRepository, times(1)).findByActorKey(actorKey);
	}

	@Test
	@DisplayName("get - Actor가 존재하지 않는 경우 예외 발생")
	void get_WhenActorNotExists_ThrowsException() {
		// given
		given(actorRepository.findByActorKey(actorKey))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> actorService.get(actorKey))
				.isInstanceOf(InsightException.class)
				.hasMessageContaining(InsightError.NOT_FOUND_ACTOR.toString());

		verify(actorRepository, times(1)).findByActorKey(actorKey);
	}

	@Test
	@DisplayName("get - null actorKey로 조회 시 예외 발생")
	void get_WhenActorKeyIsNull_ThrowsException() {
		// given
		String nullActorKey = null;
		given(actorRepository.findByActorKey(nullActorKey))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> actorService.get(nullActorKey))
				.isInstanceOf(InsightException.class);
	}

	@Test
	@DisplayName("getOrCreate - null actorKey로 생성 시도")
	void getOrCreate_WhenActorKeyIsNull_HandlesGracefully() {
		// given
		String nullActorKey = null;
		given(actorRepository.findByActorKey(nullActorKey))
				.willReturn(Optional.empty());
		given(actorRepository.save(any(Actor.class)))
				.willReturn(Actor.create(nullActorKey));

		// when
		Actor result = actorService.getOrCreate(nullActorKey);

		// then
		assertThat(result).isNotNull();
		verify(actorRepository, times(1)).save(any(Actor.class));
	}
}