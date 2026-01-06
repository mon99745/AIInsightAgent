package com.aiinsightagent.app.repository;

import com.aiinsightagent.app.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository
		extends JpaRepository<Actor, Long> {
}