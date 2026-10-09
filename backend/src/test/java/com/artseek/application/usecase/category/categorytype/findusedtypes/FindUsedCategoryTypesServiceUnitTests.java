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
class FindUsedCategoryTypesServiceUnitTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FindUsedCategoryTypesService useCase;

    @Test
    void returnsUsedTypesAsDTOs() {
        when(categoryRepository.findUsedTypes())
                .thenReturn(List.of(CategoryType.PAINTING, CategoryType.TATTOO));

        List<CategoryTypeDTO> result = useCase.execute();

        assertThat(result)
                .extracting(CategoryTypeDTO::value)
                .containsExactly("painting", "tattoo");
        verify(categoryRepository).findUsedTypes();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void returnsAnImmutableSnapshotOfTheRepositoryResult() {
        List<CategoryType> repositoryResult = new ArrayList<>();
        repositoryResult.add(CategoryType.PAINTING);
        when(categoryRepository.findUsedTypes()).thenReturn(repositoryResult);

        List<CategoryTypeDTO> result = useCase.execute();
        repositoryResult.add(CategoryType.TATTOO);

        assertThat(result)
                .extracting(CategoryTypeDTO::value)
                .containsExactly("painting");
        assertThatThrownBy(() -> result.add(new CategoryTypeDTO("tattoo")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
