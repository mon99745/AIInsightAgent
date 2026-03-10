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
@Table(name = "console_user")
public class ConsoleUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(nullable = false)
	private LocalDateTime regDate;

	protected ConsoleUser() {
	}

	public ConsoleUser(String username, String password) {
		this.username = username;
		this.password = password;
		this.regDate = LocalDateTime.now();
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}
}
