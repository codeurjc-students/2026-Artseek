package com.artseek.domain.model.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.exceptions.user.InvalidEmailException;
import com.artseek.domain.exceptions.user.InvalidPasswordHashException;
import com.artseek.domain.exceptions.user.InvalidUsernameException;
import org.junit.jupiter.api.Test;

class UserUnitTests {

    private static final String PASSWORD_HASH =
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g";
    private static final String OTHER_PASSWORD_HASH =
            "$argon2id$v=19$m=47104,t=1,p=1$b3RoZXJzYWx0b3RoZXJzYWx0$b3RoZXJoYXNob3RoZXJoYXNob3RoZXJoYXNob3RoZXI";

    @Test
    void registersAnOrdinaryUserWithNormalizedValueObjectsAndNoPersistenceState() {
        User user = User.register("  Ada ", " Ada@Example.org ", PASSWORD_HASH);

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo(new Username("ada"));
        assertThat(user.getEmail()).isEqualTo(new Email("ada@example.org"));
        assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash(PASSWORD_HASH));
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    void grantsOrdinaryPermissionsByDefault() {
        User user = User.register("ada", "ada@example.org", PASSWORD_HASH);

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isAdministrator()).isFalse();
    }

    @Test
    void createsAnAdministratorOnlyThroughTheExplicitFactory() {
        User administrator = User.createAdministrator("ada", "ada@example.org", PASSWORD_HASH);

        assertThat(administrator.getRole()).isEqualTo(Role.ADMIN);
        assertThat(administrator.isAdministrator()).isTrue();
    }

    @Test
    void rejectsInvalidOrMissingAccountData() {
        assertThatThrownBy(() -> User.register(null, "ada@example.org", PASSWORD_HASH))
                .isInstanceOf(InvalidUsernameException.class);
        assertThatThrownBy(() -> User.register("ada", "not-an-email", PASSWORD_HASH))
                .isInstanceOf(InvalidEmailException.class)
                .extracting(exception -> ((InvalidEmailException) exception).getInvalidEmail())
                .isEqualTo("not-an-email");
        assertThatThrownBy(() -> User.register("ada", "ada@example.org", "plaintext password"))
                .isInstanceOf(InvalidPasswordHashException.class);
        assertThatThrownBy(() -> User.createAdministrator("ada", "ada@example.org", null))
                .isInstanceOf(InvalidPasswordHashException.class);
    }

    @Test
    void changesThePasswordHash() {
        User user = User.register("ada", "ada@example.org", PASSWORD_HASH);

        user.changePasswordHash(OTHER_PASSWORD_HASH);

        assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash(OTHER_PASSWORD_HASH));
        assertThatThrownBy(() -> user.changePasswordHash(null))
                .isInstanceOf(InvalidPasswordHashException.class);
    }

    @Test
    void excludesCredentialsAndEmailFromItsStringRepresentation() {
        User user = User.register("ada", "ada@example.org", PASSWORD_HASH);

        assertThat(user.toString())
                .contains("ada")
                .doesNotContain(PASSWORD_HASH, "ada@example.org");
    }
}
