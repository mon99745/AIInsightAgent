package com.aiinsightagent.console.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 분석 결과 읽기 전용 엔티티 (analysis_result 테이블 매핑)
 */
@Getter
@Entity
@Immutable
@Table(name = "analysis_result")
public class ConsoleAnalysisResultView {

	private static final Map<String, String> ORDINAL_TO_TYPE = Map.of(
			"0", "STYLE",
			"1", "PATTERN",
			"2", "CLASSIFICATION"
	);

	@Id
	@Column(name = "result_id")
	private Long resultId;

	@Column(name = "request_id")
	private UUID requestId;

	@Column(name = "actor_id")
	private Long actorId;

	@Column(name = "input_id")
	private Long inputId;

	@Column(name = "analysis_type")
	private String analysisType;

	@Column(name = "analysis_version")
	private String analysisVersion;

	@Lob
	@Column(name = "result_payload", columnDefinition = "LONGTEXT")
	private String resultPayload;

	@Column(name = "status")
	private String status;

	@Column(name = "reg_date")
	private LocalDateTime regDate;

	protected ConsoleAnalysisResultView() {
	}

	@Transient
	public String getAnalysisTypeName() {
		if (analysisType == null) {
			return "-";
		}
		return ORDINAL_TO_TYPE.getOrDefault(analysisType, analysisType);
	}
}
