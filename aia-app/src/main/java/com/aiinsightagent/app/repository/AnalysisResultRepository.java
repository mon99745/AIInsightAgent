package com.aiinsightagent.app.repository;

import com.aiinsightagent.app.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultRepository
		extends JpaRepository<AnalysisResult, Long> {
}