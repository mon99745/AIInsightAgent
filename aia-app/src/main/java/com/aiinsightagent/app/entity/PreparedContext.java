package com.aiinsightagent.app.entity;

import com.aiinsightagent.app.enums.ConfidenceLevel;
import com.aiinsightagent.app.enums.ContextScope;
import com.aiinsightagent.app.enums.ContextType;
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContextType contextType;


	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContextScope contextScope;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String contextPayload;

	private String contextVersion;

	@Enumerated(EnumType.STRING)
	private ConfidenceLevel confidenceLevel;

	@Column(nullable = false)
	private boolean isActive;

	@Column(nullable = false)
	private LocalDateTime regDate;

	private LocalDateTime modDate;

	protected PreparedContext() {
	}

	public PreparedContext(Actor actor, String contextPayload) {
		this.actor = actor;
		this.contextType = ContextType.PROFILE;
		this.contextScope = ContextScope.ACTOR;
		this.contextPayload = contextPayload;
		this.isActive = true;
		this.confidenceLevel = ConfidenceLevel.MEDIUM;
		this.regDate = LocalDateTime.now();
	}

	public String asPromptText() {
		return contextPayload;
	}
}
