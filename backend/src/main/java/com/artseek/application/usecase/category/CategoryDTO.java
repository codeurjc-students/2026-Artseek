package com.artseek.application.usecase.category;

import com.artseek.domain.model.category.Category;

public record CategoryDTO(Long id, String name, String type) {

    public static CategoryDTO from(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getType().value());
    }
}
