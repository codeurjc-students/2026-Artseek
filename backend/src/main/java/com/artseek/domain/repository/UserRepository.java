package com.artseek.domain.repository;

import java.util.Optional;

import com.artseek.domain.model.user.Email;
import com.artseek.domain.model.user.User;
import com.artseek.domain.model.user.Username;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(Username username);

    Optional<User> findByEmail(Email email);

    boolean existsByUsername(Username username);

    boolean existsByEmail(Email email);

    User save(User user);
}
