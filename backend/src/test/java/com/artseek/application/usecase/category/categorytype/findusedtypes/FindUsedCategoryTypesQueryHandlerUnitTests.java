package com.artseek.application.usecase.category.categorytype.findusedtypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindUsedCategoryTypesQueryHandlerUnitTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FindUsedCategoryTypesQueryHandler handler;

    @Test
    void registersItsQueryType() {
        assertThat(handler.queryType()).isEqualTo(FindUsedCategoryTypesQuery.class);
    }

    @Test
    void mapsUsedTypesToDTOs() {
        when(categoryRepository.findUsedTypes())
                .thenReturn(List.of(CategoryType.PAINTING, CategoryType.TATTOO));

        assertThat(handler.handle(new FindUsedCategoryTypesQuery()))
                .containsExactly(new CategoryTypeDTO("painting"), new CategoryTypeDTO("tattoo"));
        verify(categoryRepository).findUsedTypes();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void returnsEmptyWhenThereAreNoUsedTypes() {
        when(categoryRepository.findUsedTypes()).thenReturn(List.of());

        assertThat(handler.handle(new FindUsedCategoryTypesQuery())).isEmpty();

        verify(categoryRepository).findUsedTypes();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void returnsAnImmutableSnapshot() {
        List<CategoryType> types = new ArrayList<>(List.of(CategoryType.PAINTING));
        when(categoryRepository.findUsedTypes()).thenReturn(types);

        List<CategoryTypeDTO> result = handler.handle(new FindUsedCategoryTypesQuery());
        types.add(CategoryType.TATTOO);

        assertThat(result).containsExactly(new CategoryTypeDTO("painting"));
        assertThatThrownBy(() -> result.add(new CategoryTypeDTO("tattoo")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
