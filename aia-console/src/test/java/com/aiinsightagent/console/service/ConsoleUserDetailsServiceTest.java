package com.aiinsightagent.console.service;

import com.aiinsightagent.console.entity.ConsoleUser;
import com.aiinsightagent.console.repository.ConsoleUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsoleUserDetailsService 테스트")
class ConsoleUserDetailsServiceTest {

	@Mock
	private ConsoleUserRepository consoleUserRepository;

	@InjectMocks
	private ConsoleUserDetailsService consoleUserDetailsService;

	@Test
	@DisplayName("loadUserByUsername - 존재하는 사용자 반환")
	void loadUserByUsername_ReturnsUserDetails() {
		ConsoleUser user = new ConsoleUser("admin", "$2a$10$hashedPassword");
		given(consoleUserRepository.findByUsername("admin")).willReturn(Optional.of(user));

		UserDetails userDetails = consoleUserDetailsService.loadUserByUsername("admin");

		assertThat(userDetails.getUsername()).isEqualTo("admin");
		assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hashedPassword");
		assertThat(userDetails.isEnabled()).isTrue();
		assertThat(userDetails.getAuthorities()).hasSize(1);
		assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
	}

	@Test
	@DisplayName("loadUserByUsername - 존재하지 않는 사용자면 UsernameNotFoundException 발생")
	void loadUserByUsername_WhenUserNotFound_ThrowsException() {
		given(consoleUserRepository.findByUsername("unknown")).willReturn(Optional.empty());

		assertThatThrownBy(() -> consoleUserDetailsService.loadUserByUsername("unknown"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("unknown");
	}

	@Test
	@DisplayName("loadUserByUsername - 비활성화된 사용자면 enabled false 반환")
	void loadUserByUsername_WhenUserDisabled_ReturnsDisabledUserDetails() {
		ConsoleUser user = new ConsoleUser("disabled", "hash");
		// 리플렉션으로 enabled를 false로 설정
		try {
			var field = ConsoleUser.class.getDeclaredField("enabled");
			field.setAccessible(true);
			field.set(user, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		given(consoleUserRepository.findByUsername("disabled")).willReturn(Optional.of(user));

		UserDetails userDetails = consoleUserDetailsService.loadUserByUsername("disabled");

		assertThat(userDetails.isEnabled()).isFalse();
	}
}
