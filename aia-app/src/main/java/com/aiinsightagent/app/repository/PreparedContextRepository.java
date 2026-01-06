package com.aiinsightagent.app.repository;

import com.aiinsightagent.app.entity.PreparedContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreparedContextRepository
		extends JpaRepository<PreparedContext, Long> {
}