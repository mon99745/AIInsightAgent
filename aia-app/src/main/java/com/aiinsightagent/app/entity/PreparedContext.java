package com.aiinsightagent.app.entity;

import com.aiinsightagent.app.enums.ConfidenceLevel;
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
@Table(name = "prepared_context")
public class PreparedContext {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long contextId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_id", nullable = false)
	private Actor actor;

	@Column(nullable = false)
	private String contextType;


	@Column(nullable = false)
	private String contextScope;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String contextPayload;

	@Enumerated(EnumType.STRING)
	private ConfidenceLevel confidenceLevel;

	@Column(nullable = false)
	private boolean isActive;

	@Column(nullable = false)
	private LocalDateTime regDate;

	private LocalDateTime modDate;

	protected PreparedContext() {
	}

	public PreparedContext(Actor actor, String contextType, String contextPayload) {
		this.actor = actor;
		this.contextType = contextType;
		this.contextScope = "ACTOR";
		this.contextPayload = contextPayload;
		this.isActive = true;
		this.confidenceLevel = ConfidenceLevel.MEDIUM;
		this.regDate = LocalDateTime.now();
	}

	public String asPromptText() {
		return contextPayload;
	}

	public void update(String category, String data) {
		this.contextType = category;
		this.contextPayload = data;
	}
}
