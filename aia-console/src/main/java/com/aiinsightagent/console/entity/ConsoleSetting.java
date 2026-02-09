package com.aiinsightagent.console.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "console_setting")
public class ConsoleSetting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long settingId;

	@Column(nullable = false, unique = true)
	private String settingKey;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String settingValue;

	private String description;

	@Column(nullable = false)
	private LocalDateTime regDate;

	private LocalDateTime modDate;

	protected ConsoleSetting() {
	}

	public ConsoleSetting(String settingKey, String settingValue, String description) {
		this.settingKey = settingKey;
		this.settingValue = settingValue;
		this.description = description;
		this.regDate = LocalDateTime.now();
	}

	public void updateValue(String newValue) {
		this.settingValue = newValue;
		this.modDate = LocalDateTime.now();
	}

	public void updateDescription(String description) {
		this.description = description;
		this.modDate = LocalDateTime.now();
	}
}
