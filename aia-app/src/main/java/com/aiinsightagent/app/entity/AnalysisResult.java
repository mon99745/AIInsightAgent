package com.aiinsightagent.app.entity;

import com.aiinsightagent.app.enums.AnalysisStatus;
import com.aiinsightagent.app.enums.AnalysisType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "analysis_result")
public class AnalysisResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long resultId;

	@Column(nullable = false, unique = true)
	private UUID requestId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_id", nullable = false)
	private Actor actor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "input_id", nullable = false)
	private AnalysisRawData analysisInput;

	@Column(nullable = false)
	private AnalysisType analysisType;

	private String analysisVersion;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String resultPayload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnalysisStatus status;

	@Column(nullable = false)
	private LocalDateTime regDate;

	protected AnalysisResult() {
	}

	public AnalysisResult(
			Actor actor,
			AnalysisRawData analysisInput,
			AnalysisType analysisType,
			AnalysisStatus status,
			String resultPayload
	) {
		this.requestId = UUID.randomUUID();
		this.actor = actor;
		this.analysisInput = analysisInput;
		this.analysisType = analysisType;
		this.resultPayload = resultPayload;
		this.status = status;
		this.regDate = LocalDateTime.now();
	}
}