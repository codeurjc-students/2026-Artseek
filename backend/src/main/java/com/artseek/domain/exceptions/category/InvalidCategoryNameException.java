package com.artseek.domain.exceptions.category;

public final class InvalidCategoryNameException extends RuntimeException {

    public static final String MESSAGE = "Category name must not be null or blank.";

    private final String invalidName;

    public InvalidCategoryNameException(String invalidName) {
        super(MESSAGE);
        this.invalidName = invalidName;
    }

    public String getInvalidName() {
        return invalidName;
    }
}
