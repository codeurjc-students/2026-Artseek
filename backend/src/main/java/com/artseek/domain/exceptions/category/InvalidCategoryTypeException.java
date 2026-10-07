package com.artseek.domain.exceptions.category;

public final class InvalidCategoryTypeException extends RuntimeException {

    public static final String MESSAGE = "Category type must be one of the supported values.";

    private final String invalidType;

    public InvalidCategoryTypeException(String invalidType) {
        super(MESSAGE);
        this.invalidType = invalidType;
    }

    public String getInvalidType() {
        return invalidType;
    }
}
