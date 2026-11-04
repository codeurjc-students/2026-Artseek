package com.artseek.domain.model.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.exceptions.user.InvalidEmailException;
import com.artseek.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailUnitTests {

    @Test
    void normalizesToTrimmedLowercase() {
        assertThat(new Email("  Ada.Lovelace@Example.ORG ").value()).isEqualTo("ada.lovelace@example.org");
    }

    @Test
    void acceptsTheMaximumLength() {
        String domain = "@example.org";
        String maximumEmail = "a".repeat(Email.MAX_LENGTH - domain.length()) + domain;

        assertThat(new Email(maximumEmail).value()).hasSize(Email.MAX_LENGTH);
    }

    @Test
    void rejectsANullEmail() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage(InvalidEmailException.MESSAGE)
                .extracting(exception -> ((InvalidEmailException) exception).getInvalidEmail())
                .isNull();
    }

    @Test
    void rejectsAnEmailLongerThanTheMaximum() {
        String domain = "@example.org";
        String tooLongEmail = "a".repeat(Email.MAX_LENGTH - domain.length() + 1) + domain;

        assertThatThrownBy(() -> new Email(tooLongEmail))
                .isInstanceOf(InvalidEmailException.class)
                .extracting(exception -> ((InvalidEmailException) exception).getInvalidEmail())
                .isEqualTo(tooLongEmail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "ada", "ada@", "@example.org", "ada@example", "ada lovelace@example.org",
            "ada@@example.org"})
    void rejectsInvalidEmailsKeepingTheRejectedInput(String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage(InvalidEmailException.MESSAGE)
                .extracting(exception -> ((InvalidEmailException) exception).getInvalidEmail())
                .isEqualTo(invalidEmail);
    }

    @Test
    void reportsTheRejectedFieldThroughTheSharedValidationType() {
        assertThatThrownBy(() -> new Email("x"))
                .isInstanceOf(DomainValidationException.class)
                .extracting(exception -> ((DomainValidationException) exception).getField())
                .isEqualTo(InvalidEmailException.FIELD)
                .isEqualTo("email");
    }
}
