package com.artseek.application.category.categorytype;

import com.artseek.domain.category.CategoryType;

public record CategoryTypeDTO(String value) {

    public static CategoryTypeDTO from(CategoryType type) {
        return new CategoryTypeDTO(type.value());
    }
}
