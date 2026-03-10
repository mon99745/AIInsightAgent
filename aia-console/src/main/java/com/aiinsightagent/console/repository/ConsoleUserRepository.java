package com.aiinsightagent.console.repository;

import com.aiinsightagent.console.entity.ConsoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsoleUserRepository extends JpaRepository<ConsoleUser, Long> {

	Optional<ConsoleUser> findByUsername(String username);

	boolean existsByUsername(String username);
}
