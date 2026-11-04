package com.artseek.application.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.domain.user.User;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserDTOUnitTests {

    private static final String PASSWORD_HASH =
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g";

    @Test
    void mapsThePublicAccountDataWithTheRoleAsItsStableValue() {
        User user = User.register("Ada", "ada@example.org", PASSWORD_HASH);

        UserDTO dto = UserDTO.from(user);

        assertThat(dto.id()).isNull();
        assertThat(dto.username()).isEqualTo("ada");
        assertThat(dto.role()).isEqualTo("user");
        assertThat(dto.createdAt()).isNull();
    }

    @Test
    void exposesNoCredentialOrPrivateContactFields() {
        assertThat(Arrays.stream(UserDTO.class.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly("id", "username", "role", "createdAt");
    }

    @Test
    void neverIncludesThePasswordHashInItsStringRepresentation() {
        User user = User.register("ada", "ada@example.org", PASSWORD_HASH);

        assertThat(UserDTO.from(user).toString())
                .doesNotContain(PASSWORD_HASH, "ada@example.org");
    }
}
