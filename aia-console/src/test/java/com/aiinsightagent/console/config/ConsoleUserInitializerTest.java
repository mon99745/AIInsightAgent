package com.aiinsightagent.console.config;

import com.aiinsightagent.console.entity.ConsoleUser;
import com.aiinsightagent.console.repository.ConsoleUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsoleUserInitializer 테스트")
class ConsoleUserInitializerTest {

	@Mock
	private ConsoleUserRepository consoleUserRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ConsoleProperties consoleProperties;

	@InjectMocks
	private ConsoleUserInitializer consoleUserInitializer;

	@Test
	@DisplayName("run - 사용자 없으면 기본 계정 생성")
	void run_WhenUserNotExists_CreatesDefaultUser() throws Exception {
		ConsoleProperties.Security security = new ConsoleProperties.Security();
		security.setUsername("admin");
		security.setPassword("admin");

		given(consoleProperties.getSecurity()).willReturn(security);
		given(consoleUserRepository.existsByUsername("admin")).willReturn(false);
		given(passwordEncoder.encode("admin")).willReturn("$2a$10$encoded");

		consoleUserInitializer.run(null);

		ArgumentCaptor<ConsoleUser> captor = ArgumentCaptor.forClass(ConsoleUser.class);
		verify(consoleUserRepository).save(captor.capture());
		assertThat(captor.getValue().getUsername()).isEqualTo("admin");
		assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$encoded");
	}

	@Test
	@DisplayName("run - 이미 사용자가 존재하면 생성하지 않음")
	void run_WhenUserExists_SkipsCreation() throws Exception {
		ConsoleProperties.Security security = new ConsoleProperties.Security();
		security.setUsername("admin");

		given(consoleProperties.getSecurity()).willReturn(security);
		given(consoleUserRepository.existsByUsername("admin")).willReturn(true);

		consoleUserInitializer.run(null);

		verify(consoleUserRepository, never()).save(any());
	}

	@Test
	@DisplayName("run - BCrypt로 비밀번호가 해시되어 저장됨")
	void run_PasswordIsEncoded() throws Exception {
		PasswordEncoder realEncoder = new BCryptPasswordEncoder();
		ConsoleProperties.Security security = new ConsoleProperties.Security();
		security.setUsername("admin");
		security.setPassword("admin");

		given(consoleProperties.getSecurity()).willReturn(security);
		given(consoleUserRepository.existsByUsername("admin")).willReturn(false);
		given(passwordEncoder.encode("admin")).willReturn(realEncoder.encode("admin"));

		consoleUserInitializer.run(null);

		ArgumentCaptor<ConsoleUser> captor = ArgumentCaptor.forClass(ConsoleUser.class);
		verify(consoleUserRepository).save(captor.capture());

		String savedPassword = captor.getValue().getPassword();
		assertThat(realEncoder.matches("admin", savedPassword)).isTrue();
	}
}
