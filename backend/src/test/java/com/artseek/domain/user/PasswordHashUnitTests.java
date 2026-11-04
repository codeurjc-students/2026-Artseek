package com.artseek.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.user.exceptions.InvalidPasswordHashException;
import com.artseek.domain.shared.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordHashUnitTests {

    private static final String ENCODED_HASH =
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHRzYWx0c2FsdA$aGFzaGhhc2hoYXNoaGFzaGhhc2hoYXNoaGFzaGhhc2g";

    @Test
    void keepsTheEncodedHashAndExposesItsVerificationMetadata() {
        PasswordHash passwordHash = new PasswordHash(ENCODED_HASH);

        assertThat(passwordHash.encoded()).isEqualTo(ENCODED_HASH);
        assertThat(passwordHash.algorithm()).isEqualTo("argon2id");
        assertThat(passwordHash.version()).isEqualTo(19);
        assertThat(passwordHash.memoryCostKib()).isEqualTo(19456);
        assertThat(passwordHash.iterations()).isEqualTo(2);
        assertThat(passwordHash.parallelism()).isEqualTo(1);
    }

    @Test
    void neverRevealsTheEncodedHashInItsStringRepresentation() {
        assertThat(new PasswordHash(ENCODED_HASH).toString())
                .isEqualTo("PasswordHash[PROTECTED]")
                .doesNotContain(ENCODED_HASH);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "correct horse battery staple",
            "$argon2i$v=19$m=19456,t=2,p=1$c2FsdA$aGFzaA",
            "$2a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ01234",
            "$argon2id$v=19$m=19456,t=2$c2FsdA$aGFzaA",
            "$argon2id$v=19$m=19456,t=2,p=1$c2FsdA"})
    void rejectsValuesThatAreNotEncodedArgon2idHashesWithoutKeepingThem(String invalidHash) {
        assertThatThrownBy(() -> new PasswordHash(invalidHash))
                .isInstanceOf(InvalidPasswordHashException.class)
                .hasMessage(InvalidPasswordHashException.MESSAGE);
    }

    @Test
    void rejectsAnEncodedHashLongerThanTheColumn() {
        String tooLongHash = ENCODED_HASH + "a".repeat(PasswordHash.MAX_LENGTH);

        assertThatThrownBy(() -> new PasswordHash(tooLongHash))
                .isInstanceOf(InvalidPasswordHashException.class);
    }

    @Test
    void reportsTheRejectedFieldThroughTheSharedValidationType() {
        assertThatThrownBy(() -> new PasswordHash("x"))
                .isInstanceOf(DomainValidationException.class)
                .extracting(exception -> ((DomainValidationException) exception).getField())
                .isEqualTo(InvalidPasswordHashException.FIELD)
                .isEqualTo("passwordHash");
    }
}
