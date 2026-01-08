package com.aiinsightagent.app.repository;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.AnalysisRawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRawDataRepository
		extends JpaRepository<AnalysisRawData, Long> {
	List<AnalysisRawData> findAllByActor(Actor actor);
}