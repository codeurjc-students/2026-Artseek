package com.artseek.domain.user.exceptions;

import com.artseek.domain.shared.DomainValidationException;

/**
 * Rejects a value that is not an encoded Argon2id hash.
 *
 * <p>Unlike other domain exceptions, the rejected value is deliberately not kept: it could be a
 * plaintext password passed by mistake and must never reach logs or error responses.
 */
public final class InvalidPasswordHashException extends DomainValidationException {

    public static final String FIELD = "passwordHash";

    public static final String MESSAGE = "Password hash must be an encoded Argon2id hash.";

    public InvalidPasswordHashException() {
        super(FIELD, MESSAGE);
    }
}
