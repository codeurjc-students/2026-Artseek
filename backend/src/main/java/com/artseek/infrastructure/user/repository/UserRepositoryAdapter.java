package com.artseek.infrastructure.user.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;
import com.artseek.domain.user.repository.UserRepository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return springDataUserRepository.findByUsername(username.value()).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmail(email.value()).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return springDataUserRepository.existsByUsername(username.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataUserRepository.existsByEmail(email.value());
    }

    @Override
    public User save(User user) {
        UserPersistenceModel saved = springDataUserRepository.save(UserMapper.toPersistence(user));
        return UserMapper.toDomain(saved);
    }
}
