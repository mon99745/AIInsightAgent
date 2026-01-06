package com.aiinsightagent.app.entity;

import com.aiinsightagent.app.enums.ActorStatus;
import com.aiinsightagent.app.enums.ActorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "actor")
public class Actor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long actorId;

	@Column(nullable = false, unique = true)
	private String actorKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ActorType actorType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ActorStatus status;

	@Column(nullable = false)
	private LocalDateTime regDate;

	private LocalDateTime modDate;

	protected Actor() {
	}

	public Actor(String actorKey, ActorType actorType, ActorStatus status) {
		this.actorKey = actorKey;
		this.actorType = actorType;
		this.status = status;
		this.regDate = LocalDateTime.now();
	}

	public static Actor create(String actorKey) {
		return new Actor(actorKey, ActorType.DEVICE, ActorStatus.ACTIVE);
	}
}
