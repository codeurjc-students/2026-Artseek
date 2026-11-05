package com.artseek.application.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.domain.user.Email;
import com.artseek.domain.user.PasswordHash;
import com.artseek.domain.user.Role;
import com.artseek.domain.user.User;
import com.artseek.domain.user.Username;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserDTOUnitTests {

    private static final String PASSWORD_HASH =
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g";

    @Test
    void mapsThePublicAccountDataWithTheRoleAsItsStableValue() {
        User user = User.register(new Username("Ada"), new Email("ada@example.org"), new PasswordHash(PASSWORD_HASH));

        UserDTO dto = UserDTO.from(user);

        assertThat(dto.id()).isNull();
        assertThat(dto.username()).isEqualTo("ada");
        assertThat(dto.role()).isEqualTo("user");
    }

    @Test
    void mapsThePersistedIdentifierOfARestoredUser() {
        User user = User.restore(7L, new Username("grace"), new Email("grace@example.org"),
                new PasswordHash(PASSWORD_HASH), Role.ADMIN);

        assertThat(UserDTO.from(user)).isEqualTo(new UserDTO(7L, "grace", "admin"));
    }

    @Test
    void exposesNoCredentialOrPrivateContactFields() {
        assertThat(Arrays.stream(UserDTO.class.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly("id", "username", "role");
    }

    @Test
    void neverIncludesThePasswordHashInItsStringRepresentation() {
        User user = User.register(new Username("ada"), new Email("ada@example.org"), new PasswordHash(PASSWORD_HASH));

        assertThat(UserDTO.from(user).toString())
                .doesNotContain(PASSWORD_HASH, "ada@example.org");
    }
}
