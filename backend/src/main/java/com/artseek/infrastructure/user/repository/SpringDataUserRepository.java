package com.artseek.infrastructure.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(Username username);

    Optional<User> findByEmail(Email email);

    boolean existsByUsername(Username username);

    boolean existsByEmail(Email email);
}
