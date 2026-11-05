package com.artseek.infrastructure.category.repository;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

/** Row shape of the {@code categories} table (V2). The migration is the source of truth. */
@Table("categories")
record CategoryPersistenceModel(
        @Id Long id,
        String name,
        String type,                         // CategoryType name, e.g. PAINTING
        // Managed by the database: DEFAULT now() and trg_categories_set_updated_at (V1, V2)
        @ReadOnlyProperty Instant createdAt,
        @ReadOnlyProperty Instant updatedAt) {

    static final String NAME_TYPE_CONSTRAINT = "uq_categories_name_type";

    /** Lets Spring Data set the generated id on the saved copy. */
    CategoryPersistenceModel withId(Long id) {
        return new CategoryPersistenceModel(id, name, type, createdAt, updatedAt);
    }
}
