package com.artseek.infrastructure.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artseek.domain.model.user.Email;
import com.artseek.domain.model.user.User;
import com.artseek.domain.model.user.Username;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(Username username);

    Optional<User> findByEmail(Email email);

    boolean existsByUsername(Username username);

    boolean existsByEmail(Email email);
}
