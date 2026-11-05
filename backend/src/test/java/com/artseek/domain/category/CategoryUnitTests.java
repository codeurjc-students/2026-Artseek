package com.artseek.domain.category;

import com.artseek.domain.shared.DomainValidationException;
import com.artseek.domain.category.exceptions.InvalidCategoryNameException;
import com.artseek.domain.category.exceptions.InvalidCategoryTypeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CategoryUnitTests {

    @Test
    void createsANewCategoryWithoutAPersistenceIdentifier() {
        Category category = new Category("  Surrealism  ", CategoryType.PAINTING);

        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isEqualTo("Surrealism");
        assertThat(category.getType()).isEqualTo(CategoryType.PAINTING);
    }

    @Test
    void restoresAnExistingCategoryWithItsIdentifier() {
        Category category = Category.restore(7L, "Surrealism", CategoryType.PAINTING);

        assertThat(category.getId()).isEqualTo(7L);
        assertThat(category.getName()).isEqualTo("Surrealism");
        assertThat(category.getType()).isEqualTo(CategoryType.PAINTING);
    }

    @Test
    void validatesRestoredValuesAgain() {
        assertThat(Category.restore(7L, "  Surrealism  ", CategoryType.PAINTING).getName())
                .isEqualTo("Surrealism");
        assertThatThrownBy(() -> Category.restore(7L, " ", CategoryType.PAINTING))
                .isInstanceOf(InvalidCategoryNameException.class)
                .extracting(exception -> ((InvalidCategoryNameException) exception).getInvalidName())
                .isEqualTo(" ");
        assertThatThrownBy(() -> Category.restore(7L, "Surrealism", null))
                .isInstanceOf(InvalidCategoryTypeException.class);
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

    @Test
    void reportsTheRejectedFieldThroughTheSharedValidationType() {
        assertThatThrownBy(() -> new Category(" ", CategoryType.PAINTING))
                .isInstanceOf(DomainValidationException.class)
                .extracting(exception -> ((DomainValidationException) exception).getField())
                .isEqualTo("name");
        assertThatThrownBy(() -> CategoryType.fromValue("unknown"))
                .isInstanceOf(DomainValidationException.class)
                .extracting(exception -> ((DomainValidationException) exception).getField())
                .isEqualTo("type");
    }
}
