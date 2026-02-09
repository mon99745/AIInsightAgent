package com.aiinsightagent.console.repository;

import com.aiinsightagent.console.entity.ConsoleAnalysisResultView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ConsoleAnalysisResultRepository extends JpaRepository<ConsoleAnalysisResultView, Long> {

	long countByStatus(String status);

	Page<ConsoleAnalysisResultView> findAllByOrderByRegDateDesc(Pageable pageable);

	Page<ConsoleAnalysisResultView> findByStatusOrderByRegDateDesc(String status, Pageable pageable);

	@Query("SELECT v FROM ConsoleAnalysisResultView v WHERE v.analysisType = :type ORDER BY v.regDate DESC")
	Page<ConsoleAnalysisResultView> findByAnalysisType(@Param("type") String type, Pageable pageable);

	@Query("SELECT v FROM ConsoleAnalysisResultView v WHERE "
			+ "(:status IS NULL OR v.status = :status) AND "
			+ "(:type IS NULL OR v.analysisType = :type) AND "
			+ "(:requestId IS NULL OR CAST(v.requestId AS string) LIKE CONCAT('%', :requestId, '%')) AND "
			+ "(:version IS NULL OR v.analysisVersion = :version) AND "
			+ "(:startDate IS NULL OR v.regDate >= :startDate) AND "
			+ "(:endDate IS NULL OR v.regDate <= :endDate) "
			+ "ORDER BY v.regDate DESC")
	Page<ConsoleAnalysisResultView> search(
			@Param("status") String status,
			@Param("type") String type,
			@Param("requestId") String requestId,
			@Param("version") String version,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			Pageable pageable);
}
