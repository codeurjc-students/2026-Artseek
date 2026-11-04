package com.artseek.infrastructure.user.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;
import com.artseek.domain.user.repository.UserRepository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springUserRepository) {
        this.springUserRepository = springUserRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return springUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return springUserRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springUserRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return springUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springUserRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return springUserRepository.save(user);
    }
}
