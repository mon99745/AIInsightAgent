package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.repository.ActorRepository;
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
}