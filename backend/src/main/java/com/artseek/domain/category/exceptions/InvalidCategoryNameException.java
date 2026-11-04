package com.artseek.domain.category.exceptions;

import com.artseek.domain.shared.DomainValidationException;

public final class InvalidCategoryNameException extends DomainValidationException {

    public static final String FIELD = "name";

    public static final String MESSAGE = "Category name must not be null or blank.";

    private final String invalidName;

    public InvalidCategoryNameException(String invalidName) {
        super(FIELD, MESSAGE);
        this.invalidName = invalidName;
    }

    public String getInvalidName() {
        return invalidName;
    }
}
