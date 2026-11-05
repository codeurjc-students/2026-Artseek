package com.artseek.infrastructure.category.repository;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;

/** Converts between the {@link Category} aggregate and its table row. */
final class CategoryMapper {

    private CategoryMapper() {
    }

    static CategoryPersistenceModel toPersistence(Category category) {
        // Timestamps are read-only: the database fills them and Spring Data never writes them.
        return new CategoryPersistenceModel(
                category.getId(), category.getName(), category.getType().name(), null, null);
    }

    static Category toDomain(CategoryPersistenceModel model) {
        return Category.restore(model.id(), model.name(), CategoryType.valueOf(model.type()));
    }
}
