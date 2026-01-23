package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.repository.ActorRepository;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActorService {
	private final ActorRepository actorRepository;

	public Actor getOrCreate(String actorKey) {
		return actorRepository.findByActorKey(actorKey)
				.orElseGet(() -> actorRepository.save(
						Actor.create(actorKey)
				));
	}

	public Actor get(String actorKey) {
		return actorRepository.findByActorKey(actorKey)
				.orElseThrow(() -> new InsightException(InsightError.NOT_FOUND_ACTOR, ":" + actorKey));
	}
}