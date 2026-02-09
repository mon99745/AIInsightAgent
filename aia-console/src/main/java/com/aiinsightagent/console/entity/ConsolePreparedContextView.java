package com.aiinsightagent.console.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * PreparedContext 읽기 전용 엔티티 (prepared_context 테이블 매핑)
 */
@Getter
@Entity
@Immutable
@Table(name = "prepared_context")
public class ConsolePreparedContextView {

	@Id
	@Column(name = "context_id")
	private Long contextId;

	@Column(name = "actor_id")
	private Long actorId;

	@Column(name = "context_type")
	private String contextType;

	@Column(name = "context_scope")
	private String contextScope;

	@Lob
	@Column(name = "context_payload", columnDefinition = "LONGTEXT")
	private String contextPayload;

	@Column(name = "confidence_level")
	private String confidenceLevel;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "reg_date")
	private LocalDateTime regDate;

	@Column(name = "mod_date")
	private LocalDateTime modDate;

	protected ConsolePreparedContextView() {
	}
}
