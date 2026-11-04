package com.artseek.domain.exceptions.user;

import com.artseek.domain.exceptions.DomainValidationException;

public final class InvalidRoleException extends DomainValidationException {

    public static final String FIELD = "role";

    public static final String MESSAGE = "Role must be one of the supported roles.";

    private final String invalidRole;

    public InvalidRoleException(String invalidRole) {
        super(FIELD, MESSAGE);
        this.invalidRole = invalidRole;
    }

    public String getInvalidRole() {
        return invalidRole;
    }
}
