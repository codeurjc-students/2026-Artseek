package com.artseek.domain.exceptions.user;

import com.artseek.domain.exceptions.DomainValidationException;

public final class InvalidEmailException extends DomainValidationException {

    public static final String FIELD = "email";

    public static final String MESSAGE = "Email must be a valid address of at most 254 characters.";

    private final String invalidEmail;

    public InvalidEmailException(String invalidEmail) {
        super(FIELD, MESSAGE);
        this.invalidEmail = invalidEmail;
    }

    public String getInvalidEmail() {
        return invalidEmail;
    }
}
