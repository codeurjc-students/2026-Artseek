package com.artseek.infrastructure.user.repository;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

interface SpringDataUserRepository extends ListCrudRepository<UserPersistenceModel, Long> {

    Optional<UserPersistenceModel> findByUsername(String username);

    Optional<UserPersistenceModel> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
