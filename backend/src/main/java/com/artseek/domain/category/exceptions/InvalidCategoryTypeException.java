package com.artseek.domain.category.exceptions;

import com.artseek.domain.shared.DomainValidationException;

public final class InvalidCategoryTypeException extends DomainValidationException {

    public static final String FIELD = "type";

    public static final String MESSAGE = "Category type must be one of the supported values.";

    private final String invalidType;

    public InvalidCategoryTypeException(String invalidType) {
        super(FIELD, MESSAGE);
        this.invalidType = invalidType;
    }

    public String getInvalidType() {
        return invalidType;
    }
}
