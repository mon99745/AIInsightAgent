package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.repository.PreparedContextRepository;
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
}