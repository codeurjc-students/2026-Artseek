package com.artseek.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.user.exceptions.InvalidUsernameException;
import com.artseek.domain.shared.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UsernameUnitTests {

    @Test
    void normalizesToTrimmedLowercase() {
        assertThat(new Username("  Ada.Lovelace_1-x  ").value()).isEqualTo("ada.lovelace_1-x");
    }

    @Test
    void acceptsTheLengthBoundaries() {
        assertThat(new Username("abc").value()).hasSize(Username.MIN_LENGTH);
        assertThat(new Username("a".repeat(Username.MAX_LENGTH)).value()).hasSize(Username.MAX_LENGTH);
    }

    @Test
    void equalUsernamesDifferingOnlyInCaseAreEqual() {
        assertThat(new Username("Ada")).isEqualTo(new Username("ada"));
    }

    @Test
    void rejectsANullUsername() {
        assertThatThrownBy(() -> new Username(null))
                .isInstanceOf(InvalidUsernameException.class)
                .hasMessage(InvalidUsernameException.MESSAGE)
                .extracting(exception -> ((InvalidUsernameException) exception).getInvalidUsername())
                .isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "ab", "with space", "ada@home", "ádá", "tooooooooooooooooooooooooolong1"})
    void rejectsInvalidUsernamesKeepingTheRejectedInput(String invalidUsername) {
        assertThatThrownBy(() -> new Username(invalidUsername))
                .isInstanceOf(InvalidUsernameException.class)
                .hasMessage(InvalidUsernameException.MESSAGE)
                .extracting(exception -> ((InvalidUsernameException) exception).getInvalidUsername())
                .isEqualTo(invalidUsername);
    }

    @Test
    void reportsTheRejectedFieldThroughTheSharedValidationType() {
        assertThatThrownBy(() -> new Username("x"))
                .isInstanceOf(DomainValidationException.class)
                .extracting(exception -> ((DomainValidationException) exception).getField())
                .isEqualTo(InvalidUsernameException.FIELD)
                .isEqualTo("username");
    }
}
