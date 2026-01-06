package com.aiinsightagent.app.repository;

import com.aiinsightagent.app.entity.AnalysisRawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRawDataRepository
		extends JpaRepository<AnalysisRawData, Long> {
}