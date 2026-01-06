package com.aiinsightagent.app.repository;

import com.aiinsightagent.app.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActorRepository
		extends JpaRepository<Actor, Long> {
	boolean existsByActorKey(String actorKey);

	Optional<Actor> findByActorKey(String actorKey);
}