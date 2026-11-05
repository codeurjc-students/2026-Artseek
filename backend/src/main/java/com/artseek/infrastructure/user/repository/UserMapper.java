package com.artseek.infrastructure.user.repository;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.PasswordHash;
import com.artseek.domain.user.Role;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;

/** Converts between the {@link User} aggregate and its table row. */
final class UserMapper {

    private UserMapper() {
    }

    static UserPersistenceModel toPersistence(User user) {
        // Timestamps are read-only: the database fills them and Spring Data never writes them.
        return new UserPersistenceModel(
                user.getId(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getPasswordHash().encoded(),
                user.getRole().name(),
                null,
                null);
    }

    static User toDomain(UserPersistenceModel model) {
        return User.restore(
                model.id(),
                new Username(model.username()),
                new Email(model.email()),
                new PasswordHash(model.passwordHash()),
                Role.valueOf(model.role()));
    }
}
