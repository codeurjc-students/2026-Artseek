package com.artseek.infrastructure.category.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;
import com.artseek.domain.category.exceptions.InvalidCategoryNameException;
import org.junit.jupiter.api.Test;

class CategoryMapperUnitTests {

    @Test
    void mapsANewCategoryToARowWithoutIdOrTimestamps() {
        CategoryPersistenceModel model = CategoryMapper.toPersistence(new Category("Surrealism", CategoryType.PAINTING));

        assertThat(model).isEqualTo(new CategoryPersistenceModel(null, "Surrealism", "PAINTING", null, null));
    }

    @Test
    void keepsTheIdOfAnExistingCategory() {
        CategoryPersistenceModel model = CategoryMapper.toPersistence(
                Category.restore(7L, "Blackwork", CategoryType.TATTOO));

        assertThat(model.id()).isEqualTo(7L);
        assertThat(model.type()).isEqualTo("TATTOO");
    }

    @Test
    void storesTheEnumNameNotThePublicValue() {
        for (CategoryType type : CategoryType.values()) {
            assertThat(CategoryMapper.toPersistence(new Category("Any", type)).type()).isEqualTo(type.name());
        }
    }

    @Test
    void restoresACategoryFromARow() {
        Category category = CategoryMapper.toDomain(
                new CategoryPersistenceModel(7L, "Surrealism", "PAINTING", null, null));

        assertThat(category.getId()).isEqualTo(7L);
        assertThat(category.getName()).isEqualTo("Surrealism");
        assertThat(category.getType()).isEqualTo(CategoryType.PAINTING);
    }

    @Test
    void rejectsAStoredNameThatBreaksTheDomainRules() {
        assertThatThrownBy(() -> CategoryMapper.toDomain(
                new CategoryPersistenceModel(7L, " ", "PAINTING", null, null)))
                .isInstanceOf(InvalidCategoryNameException.class);
    }

    @Test
    void rejectsAnUnknownStoredType() {
        assertThatThrownBy(() -> CategoryMapper.toDomain(
                new CategoryPersistenceModel(7L, "Surrealism", "painting", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
