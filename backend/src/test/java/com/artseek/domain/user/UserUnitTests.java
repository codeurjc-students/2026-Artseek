package com.artseek.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.user.exceptions.InvalidRoleException;
import org.junit.jupiter.api.Test;

class UserUnitTests {

    private static final Username USERNAME = new Username("ada");
    private static final Email EMAIL = new Email("ada@example.org");
    private static final PasswordHash PASSWORD_HASH = new PasswordHash(
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g");
    private static final PasswordHash OTHER_PASSWORD_HASH = new PasswordHash(
            "$argon2id$v=19$m=47104,t=1,p=1$b3RoZXJzYWx0b3RoZXJzYWx0$b3RoZXJoYXNob3RoZXJoYXNob3RoZXJoYXNob3RoZXI");

    @Test
    void registersAnOrdinaryUserWithoutPersistenceState() {
        User user = User.register(USERNAME, EMAIL, PASSWORD_HASH);

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
    }

    @Test
    void restoresAnExistingUserWithItsIdentifierAndRole() {
        User user = User.restore(7L, USERNAME, EMAIL, PASSWORD_HASH, Role.ADMIN);

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isAdministrator()).isTrue();
    }

    @Test
    void rejectsAMissingRoleWhenRestoring() {
        assertThatThrownBy(() -> User.restore(7L, USERNAME, EMAIL, PASSWORD_HASH, null))
                .isInstanceOf(InvalidRoleException.class)
                .extracting(exception -> ((InvalidRoleException) exception).getInvalidRole())
                .isNull();
    }

    @Test
    void grantsOrdinaryPermissionsByDefault() {
        User user = User.register(USERNAME, EMAIL, PASSWORD_HASH);

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isAdministrator()).isFalse();
    }

    @Test
    void createsAnAdministratorOnlyThroughTheExplicitFactory() {
        User administrator = User.createAdministrator(USERNAME, EMAIL, PASSWORD_HASH);

        assertThat(administrator.getRole()).isEqualTo(Role.ADMIN);
        assertThat(administrator.isAdministrator()).isTrue();
    }

    @Test
    void changesThePasswordHash() {
        User user = User.register(USERNAME, EMAIL, PASSWORD_HASH);

        user.changePasswordHash(OTHER_PASSWORD_HASH);

        assertThat(user.getPasswordHash()).isEqualTo(OTHER_PASSWORD_HASH);
    }

    @Test
    void excludesCredentialsAndEmailFromItsStringRepresentation() {
        User user = User.register(USERNAME, EMAIL, PASSWORD_HASH);

        assertThat(user.toString())
                .contains("ada")
                .doesNotContain(PASSWORD_HASH.encoded(), "ada@example.org");
    }
}
