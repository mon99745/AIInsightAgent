package com.aiinsightagent.app.repository;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreparedContextRepository
		extends JpaRepository<PreparedContext, Long> {
	Optional<PreparedContext> findByActor(Actor actor);
}