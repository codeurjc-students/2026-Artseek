package com.artseek.application.category.findbytype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.artseek.application.category.CategoryDTO;
import com.artseek.application.category.findbytype.FindCategoriesByTypeQuery;
import com.artseek.application.category.findbytype.FindCategoriesByTypeQueryHandler;
import com.artseek.domain.exceptions.category.InvalidCategoryTypeException;
import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindCategoriesByTypeQueryHandlerUnitTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FindCategoriesByTypeQueryHandler handler;

    @Test
    void registersItsQueryType() {
        assertThat(handler.queryType()).isEqualTo(FindCategoriesByTypeQuery.class);
    }

    @Test
    void mapsMatchingCategoriesToDTOs() {
        when(categoryRepository.findByType(CategoryType.PAINTING)).thenReturn(List.of(
                new Category("Surrealism", CategoryType.PAINTING),
                new Category("Impressionism", CategoryType.PAINTING)));

        assertThat(handler.handle(new FindCategoriesByTypeQuery("painting")))
                .containsExactly(
                        new CategoryDTO(null, "Surrealism", "painting"),
                        new CategoryDTO(null, "Impressionism", "painting"));
        verify(categoryRepository).findByType(CategoryType.PAINTING);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void normalizesTypeAndReturnsEmptyWhenNothingMatches() {
        when(categoryRepository.findByType(CategoryType.TATTOO)).thenReturn(List.of());

        assertThat(handler.handle(new FindCategoriesByTypeQuery(" TATTOO "))).isEmpty();

        verify(categoryRepository).findByType(CategoryType.TATTOO);
        verifyNoMoreInteractions(categoryRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "sculpture"})
    void rejectsInvalidTypesBeforeAccessingPersistence(String type) {
        assertThatThrownBy(() -> handler.handle(new FindCategoriesByTypeQuery(type)))
                .isInstanceOf(InvalidCategoryTypeException.class)
                .hasMessage(InvalidCategoryTypeException.MESSAGE)
                .satisfies(exception -> assertThat(
                        ((InvalidCategoryTypeException) exception).getInvalidType()).isEqualTo(type));
        verifyNoInteractions(categoryRepository);
    }
}
