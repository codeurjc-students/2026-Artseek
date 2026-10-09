package com.artseek.domain.model.category;

import com.artseek.domain.exceptions.category.InvalidCategoryNameException;
import com.artseek.domain.exceptions.category.InvalidCategoryTypeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CategoryTests {

    @Test
    void createsANewCategoryWithoutAPersistenceIdentifier() {
        Category category = new Category("  Surrealism  ", CategoryType.PAINTING);

        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isEqualTo("Surrealism");
        assertThat(category.getType()).isEqualTo(CategoryType.PAINTING);
    }

    @Test
    void rejectsAnInvalidName() {
        assertThatThrownBy(() -> new Category(null, CategoryType.PAINTING))
                .isInstanceOf(InvalidCategoryNameException.class)
                .hasMessage(InvalidCategoryNameException.MESSAGE)
                .extracting(exception -> ((InvalidCategoryNameException) exception).getInvalidName())
                .isNull();
        assertThatThrownBy(() -> new Category("  ", CategoryType.PAINTING))
                .isInstanceOf(InvalidCategoryNameException.class)
                .hasMessage(InvalidCategoryNameException.MESSAGE)
                .extracting(exception -> ((InvalidCategoryNameException) exception).getInvalidName())
                .isEqualTo("  ");
    }

    @Test
    void rejectsAMissingType() {
        assertThatThrownBy(() -> new Category("Surrealism", null))
                .isInstanceOf(InvalidCategoryTypeException.class)
                .hasMessage(InvalidCategoryTypeException.MESSAGE)
                .extracting(exception -> ((InvalidCategoryTypeException) exception).getInvalidType())
                .isNull();
    }

    @Test
    void mapsTheTypeToAndFromItsStableStringValue() {
        assertThat(CategoryType.PAINTING.value()).isEqualTo("painting");
        assertThat(CategoryType.fromValue(" PAINTING ")).isEqualTo(CategoryType.PAINTING);
        assertThatThrownBy(() -> CategoryType.fromValue("unknown"))
                .isInstanceOf(InvalidCategoryTypeException.class)
                .hasMessage(InvalidCategoryTypeException.MESSAGE)
                .extracting(exception -> ((InvalidCategoryTypeException) exception).getInvalidType())
                .isEqualTo("unknown");
    }
}
