package com.artseek.infrastructure.repository.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterUnitTests {

    @Mock
    private SpringDataCategoryRepository springDataCategoryRepository;

    @InjectMocks
    private CategoryRepositoryAdapter categoryRepositoryAdapter;

    @Test
    void delegatesFindByIdToSpringDataRepository() {
        Category category = new Category("Surrealism", CategoryType.PAINTING);
        Optional<Category> expectedCategory = Optional.of(category);
        when(springDataCategoryRepository.findById(1L)).thenReturn(expectedCategory);

        Optional<Category> result = categoryRepositoryAdapter.findById(1L);

        assertThat(result).isSameAs(expectedCategory);
        verify(springDataCategoryRepository).findById(1L);
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void delegatesFindByTypeToSpringDataRepository() {
        List<Category> expectedCategories = List.of(
                new Category("Surrealism", CategoryType.PAINTING),
                new Category("Impressionism", CategoryType.PAINTING));
        when(springDataCategoryRepository.findByType(CategoryType.PAINTING))
                .thenReturn(expectedCategories);

        List<Category> result = categoryRepositoryAdapter.findByType(CategoryType.PAINTING);

        assertThat(result).isSameAs(expectedCategories);
        verify(springDataCategoryRepository).findByType(CategoryType.PAINTING);
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void delegatesFindByNameAndTypeToSpringDataRepository() {
        Category category = new Category("Surrealism", CategoryType.PAINTING);
        Optional<Category> expectedCategory = Optional.of(category);
        when(springDataCategoryRepository.findByNameAndType(
                "Surrealism", CategoryType.PAINTING))
                .thenReturn(expectedCategory);

        Optional<Category> result = categoryRepositoryAdapter.findByNameAndType(
                "Surrealism", CategoryType.PAINTING);

        assertThat(result).isSameAs(expectedCategory);
        verify(springDataCategoryRepository).findByNameAndType(
                "Surrealism", CategoryType.PAINTING);
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void delegatesSaveToSpringDataRepository() {
        Category category = new Category("Surrealism", CategoryType.PAINTING);
        when(springDataCategoryRepository.save(category)).thenReturn(category);

        Category result = categoryRepositoryAdapter.save(category);

        assertThat(result).isSameAs(category);
        verify(springDataCategoryRepository).save(category);
        verifyNoMoreInteractions(springDataCategoryRepository);
    }
}
