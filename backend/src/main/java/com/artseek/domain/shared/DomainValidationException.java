package com.artseek.domain.shared;

/**
 * Base type for domain validation failures. Each subclass names the rejected field and keeps a
 * fixed message, so the API boundary can translate every subclass through a single handler.
 */
public abstract class DomainValidationException extends RuntimeException {

    private final String field;

    protected DomainValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
