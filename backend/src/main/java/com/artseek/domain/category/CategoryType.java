package com.artseek.domain.category;

import com.artseek.domain.category.exceptions.InvalidCategoryTypeException;
import java.util.Arrays;

/** The closed set of art types recognized by the domain. */
public enum CategoryType {
    PAINTING("painting"),
    TATTOO("tattoo");

    private final String value;

    CategoryType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CategoryType fromValue(String value) {
        if (value == null) {
            throw new InvalidCategoryTypeException(null);
        }

        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidCategoryTypeException(value));
    }
}
