package com.aiinsightagent.app.entity;

import com.aiinsightagent.app.enums.InputType;
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

@Getter
@Entity
@Table(name = "analysis_row_data")
public class AnalysisRawData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long inputId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_id", nullable = false)
	private Actor actor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InputType inputType;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String rawPayload;

	@Column(nullable = false)
	private LocalDateTime regDate;

	protected AnalysisRawData() {
	}

	public AnalysisRawData(Actor actor, InputType inputType, String rawPayload) {
		this.actor = actor;
		this.inputType = inputType;
		this.rawPayload = rawPayload;
		this.regDate = LocalDateTime.now();
	}
}
