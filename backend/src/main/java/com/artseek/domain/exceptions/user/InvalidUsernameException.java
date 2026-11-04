package com.artseek.domain.exceptions.user;

import com.artseek.domain.exceptions.DomainValidationException;

public final class InvalidUsernameException extends DomainValidationException {

    public static final String FIELD = "username";

    public static final String MESSAGE = "Username must be 3 to 30 characters long and contain only "
            + "letters, digits, dots, underscores or hyphens.";

    private final String invalidUsername;

    public InvalidUsernameException(String invalidUsername) {
        super(FIELD, MESSAGE);
        this.invalidUsername = invalidUsername;
    }

    public String getInvalidUsername() {
        return invalidUsername;
    }
}
