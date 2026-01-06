package com.aiinsightagent.app.entity;

import com.aiinsightagent.app.enums.ActorStatus;
import com.aiinsightagent.app.enums.ActorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "actor")
public class Actor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long actorId;

	@Column(nullable = false)
	private ActorType actorType;

	@Column(nullable = false, unique = true)
	private String actorKey;

	@Column(nullable = false)
	private ActorStatus status;

	@Column(nullable = false)
	private LocalDateTime regDate;

	private LocalDateTime modDate;

	protected Actor() {
	}

	public Actor(ActorType actorType, String actorKey, ActorStatus status) {
		this.actorType = actorType;
		this.actorKey = actorKey;
		this.status = status;
		this.regDate = LocalDateTime.now();
	}
}
