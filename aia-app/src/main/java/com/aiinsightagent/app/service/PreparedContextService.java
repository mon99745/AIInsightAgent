package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.repository.PreparedContextRepository;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreparedContextService {
	private final PreparedContextRepository contextRepository;

	public Optional<PreparedContext> findByActorKey(Actor actor) {
		return contextRepository.findByActor(actor);
	}

	public PreparedContext save(Actor actor, Context context) {
		contextRepository.findByActor(actor)
				.ifPresent(existingContext -> {
					throw new InsightException(InsightError.EXIST_ACTOR_PREPARED_CONTEXT + ":" + actor.getActorKey());
				});

		PreparedContext preparedContext =
				new PreparedContext(actor, context.getCategory(), context.getData().toString());

		return contextRepository.save(preparedContext);
	}

	public PreparedContext get(Actor actor) {
		return contextRepository.findByActor(actor)
				.orElseThrow(() -> new InsightException(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT + ":" + actor.getActorKey()));
	}

	public PreparedContext update(Actor actor, Context context) {
		PreparedContext preparedContext = get(actor);

		preparedContext.update(context.getCategory(), context.getData().toString());

		return contextRepository.save(preparedContext);
	}

	public void delete(Actor actor) {
		contextRepository.deleteById(get(actor).getContextId());
	}
}