package com.aiinsightagent.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

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
	private String contextType;     // PROFILE, BASELINE, PREFERENCE

	@Column(nullable = false)
	private String contextScope;    // GLOBAL, ACTOR, SESSION

	@Lob
	@Column(nullable = false)
	private String contextPayload;  // JSON

	private String contextVersion;

	private String confidenceLevel; // LOW, MEDIUM, HIGH

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
		this.regDate = LocalDateTime.now();
	}
}
