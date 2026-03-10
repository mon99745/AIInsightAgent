package com.aiinsightagent.console.config;

import com.aiinsightagent.console.entity.ConsoleUser;
import com.aiinsightagent.console.repository.ConsoleUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsoleUserInitializer implements ApplicationRunner {

	private final ConsoleUserRepository consoleUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final ConsoleProperties consoleProperties;

	@Override
	public void run(ApplicationArguments args) {
		String username = consoleProperties.getSecurity().getUsername();
		if (!consoleUserRepository.existsByUsername(username)) {
			String rawPassword = consoleProperties.getSecurity().getPassword();
			ConsoleUser admin = new ConsoleUser(username, passwordEncoder.encode(rawPassword));
			consoleUserRepository.save(admin);
			log.info("Default console user created: {}", username);
		}
	}
}
