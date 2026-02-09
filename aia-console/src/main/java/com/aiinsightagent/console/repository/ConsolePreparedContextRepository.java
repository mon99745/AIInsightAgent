package com.aiinsightagent.console.repository;

import com.aiinsightagent.console.entity.ConsolePreparedContextView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsolePreparedContextRepository extends JpaRepository<ConsolePreparedContextView, Long> {

	List<ConsolePreparedContextView> findByActorIdAndActiveTrue(Long actorId);
}
