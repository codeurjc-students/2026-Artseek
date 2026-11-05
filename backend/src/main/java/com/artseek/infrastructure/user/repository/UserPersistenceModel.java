package com.artseek.infrastructure.user.repository;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

/** Row shape of the {@code users} table (V3). The migration is the source of truth. */
@Table("users")
record UserPersistenceModel(
        @Id Long id,
        String username,
        String email,
        String passwordHash,
        String role,                         // Role name, e.g. USER
        // Managed by the database: DEFAULT now() and trg_users_set_updated_at (V1, V3)
        @ReadOnlyProperty Instant createdAt,
        @ReadOnlyProperty Instant updatedAt) {

    static final String USERNAME_CONSTRAINT = "uq_users_username";
    static final String EMAIL_CONSTRAINT = "uq_users_email";

    /** Lets Spring Data set the generated id on the saved copy. */
    UserPersistenceModel withId(Long id) {
        return new UserPersistenceModel(id, username, email, passwordHash, role, createdAt, updatedAt);
    }
}
