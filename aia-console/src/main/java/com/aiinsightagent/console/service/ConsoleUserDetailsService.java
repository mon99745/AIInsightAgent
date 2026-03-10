package com.aiinsightagent.console.service;

import com.aiinsightagent.console.entity.ConsoleUser;
import com.aiinsightagent.console.repository.ConsoleUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsoleUserDetailsService implements UserDetailsService {

	private final ConsoleUserRepository consoleUserRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		ConsoleUser user = consoleUserRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		return new User(
				user.getUsername(),
				user.getPassword(),
				user.isEnabled(),
				true, true, true,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
		);
	}
}
