package com.artseek.application.usecase.category.findbytype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.artseek.application.usecase.category.CategoryDTO;
import com.artseek.domain.exceptions.category.InvalidCategoryTypeException;
import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindCategoriesByTypeServiceUnitTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FindCategoriesByTypeService useCase;

    @Test
    void returnsMatchingCategoriesAsDTOs() {
        when(categoryRepository.findByType(CategoryType.PAINTING)).thenReturn(List.of(
                new Category("Surrealism", CategoryType.PAINTING),
                new Category("Impressionism", CategoryType.PAINTING)));

        List<CategoryDTO> result = useCase.execute("painting");

        assertThat(result)
                .extracting(CategoryDTO::name, CategoryDTO::type)
                .containsExactly(
                        tuple("Surrealism", "painting"),
                        tuple("Impressionism", "painting"));
        verify(categoryRepository).findByType(CategoryType.PAINTING);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void acceptsCaseInsensitiveTypeValuesWithSurroundingWhitespace() {
        when(categoryRepository.findByType(CategoryType.PAINTING)).thenReturn(List.of());

        assertThat(useCase.execute(" PAINTING ")).isEmpty();

        verify(categoryRepository).findByType(CategoryType.PAINTING);
    }

    @Test
    void rejectsAnUnsupportedTypeBeforeAccessingTheRepository() {
        assertThatThrownBy(() -> useCase.execute("sculpture"))
                .isInstanceOf(InvalidCategoryTypeException.class)
                .hasMessage(InvalidCategoryTypeException.MESSAGE)
                .extracting(exception -> ((InvalidCategoryTypeException) exception).getInvalidType())
                .isEqualTo("sculpture");
        verifyNoInteractions(categoryRepository);
    }
}
