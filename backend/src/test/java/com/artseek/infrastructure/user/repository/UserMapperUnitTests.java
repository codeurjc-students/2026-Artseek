package com.artseek.infrastructure.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.PasswordHash;
import com.artseek.domain.user.Role;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;
import com.artseek.domain.user.exceptions.InvalidEmailException;
import com.artseek.domain.user.exceptions.InvalidPasswordHashException;
import com.artseek.domain.user.exceptions.InvalidUsernameException;
import org.junit.jupiter.api.Test;

class UserMapperUnitTests {

    private static final String PASSWORD_HASH =
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g";

    @Test
    void mapsANewUserToARowWithoutIdOrTimestamps() {
        User user = User.register(new Username("Ada"), new Email("Ada@Example.org"), new PasswordHash(PASSWORD_HASH));

        assertThat(UserMapper.toPersistence(user)).isEqualTo(
                new UserPersistenceModel(null, "ada", "ada@example.org", PASSWORD_HASH, "USER", null, null));
    }

    @Test
    void storesTheRoleNameAndKeepsTheIdOfAnExistingUser() {
        User administrator = User.restore(7L, new Username("grace"), new Email("grace@example.org"),
                new PasswordHash(PASSWORD_HASH), Role.ADMIN);

        UserPersistenceModel model = UserMapper.toPersistence(administrator);

        assertThat(model.id()).isEqualTo(7L);
        assertThat(model.role()).isEqualTo("ADMIN");
    }

    @Test
    void restoresAUserFromARow() {
        User user = UserMapper.toDomain(
                new UserPersistenceModel(7L, "ada", "ada@example.org", PASSWORD_HASH, "ADMIN", null, null));

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getUsername()).isEqualTo(new Username("ada"));
        assertThat(user.getEmail()).isEqualTo(new Email("ada@example.org"));
        assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash(PASSWORD_HASH));
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void rejectsStoredValuesThatBreakTheValueObjectRules() {
        assertThatThrownBy(() -> UserMapper.toDomain(
                new UserPersistenceModel(7L, "a", "ada@example.org", PASSWORD_HASH, "USER", null, null)))
                .isInstanceOf(InvalidUsernameException.class);
        assertThatThrownBy(() -> UserMapper.toDomain(
                new UserPersistenceModel(7L, "ada", "not-an-email", PASSWORD_HASH, "USER", null, null)))
                .isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> UserMapper.toDomain(
                new UserPersistenceModel(7L, "ada", "ada@example.org", "plaintext", "USER", null, null)))
                .isInstanceOf(InvalidPasswordHashException.class);
    }

    @Test
    void rejectsAnUnknownStoredRole() {
        assertThatThrownBy(() -> UserMapper.toDomain(
                new UserPersistenceModel(7L, "ada", "ada@example.org", PASSWORD_HASH, "user", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
