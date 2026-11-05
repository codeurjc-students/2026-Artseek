package com.artseek.infrastructure.category.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterUnitTests {

    private static final CategoryPersistenceModel SURREALISM =
            new CategoryPersistenceModel(1L, "Surrealism", "PAINTING", null, null);
    private static final CategoryPersistenceModel BLACKWORK =
            new CategoryPersistenceModel(2L, "Blackwork", "TATTOO", null, null);

    @Mock
    private SpringDataCategoryRepository springDataCategoryRepository;

    @InjectMocks
    private CategoryRepositoryAdapter categoryRepositoryAdapter;

    @Test
    void mapsTheRowFoundById() {
        when(springDataCategoryRepository.findById(1L)).thenReturn(Optional.of(SURREALISM));

        Optional<Category> result = categoryRepositoryAdapter.findById(1L);

        assertThat(result).get().satisfies(category -> {
            assertThat(category.getId()).isEqualTo(1L);
            assertThat(category.getName()).isEqualTo("Surrealism");
            assertThat(category.getType()).isEqualTo(CategoryType.PAINTING);
        });
        verify(springDataCategoryRepository).findById(1L);
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void returnsEmptyWhenNoRowHasTheId() {
        when(springDataCategoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThat(categoryRepositoryAdapter.findById(9L)).isEmpty();
    }

    @Test
    void queriesByTheStoredTypeNameAndMapsTheRows() {
        when(springDataCategoryRepository.findByType("PAINTING")).thenReturn(List.of(SURREALISM));

        List<Category> result = categoryRepositoryAdapter.findByType(CategoryType.PAINTING);

        assertThat(result).extracting(Category::getId, Category::getName)
                .containsExactly(tuple(1L, "Surrealism"));
        verify(springDataCategoryRepository).findByType("PAINTING");
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void mapsStoredTypeNamesToUsedTypesInTheSameOrder() {
        when(springDataCategoryRepository.findUsedTypes()).thenReturn(List.of("PAINTING", "TATTOO"));

        List<CategoryType> result = categoryRepositoryAdapter.findUsedTypes();

        assertThat(result).containsExactly(CategoryType.PAINTING, CategoryType.TATTOO);
        verify(springDataCategoryRepository).findUsedTypes();
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void checksExistenceByNameAndTheStoredTypeName() {
        when(springDataCategoryRepository.existsByNameAndType("Surrealism", "PAINTING")).thenReturn(true);

        boolean result = categoryRepositoryAdapter.existsByNameAndType("Surrealism", CategoryType.PAINTING);

        assertThat(result).isTrue();
        verify(springDataCategoryRepository).existsByNameAndType("Surrealism", "PAINTING");
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void savesTheMappedRowAndReturnsTheSavedCategory() {
        CategoryPersistenceModel unsaved = new CategoryPersistenceModel(null, "Surrealism", "PAINTING", null, null);
        when(springDataCategoryRepository.save(unsaved)).thenReturn(SURREALISM);

        Category result = categoryRepositoryAdapter.save(new Category("Surrealism", CategoryType.PAINTING));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Surrealism");
        verify(springDataCategoryRepository).save(unsaved);
        verifyNoMoreInteractions(springDataCategoryRepository);
    }

    @Test
    void savesAllMappedRowsAndReturnsTheSavedCategories() {
        List<CategoryPersistenceModel> unsaved = List.of(
                new CategoryPersistenceModel(null, "Surrealism", "PAINTING", null, null),
                new CategoryPersistenceModel(null, "Blackwork", "TATTOO", null, null));
        when(springDataCategoryRepository.saveAll(unsaved)).thenReturn(List.of(SURREALISM, BLACKWORK));

        List<Category> result = categoryRepositoryAdapter.saveAll(List.of(
                new Category("Surrealism", CategoryType.PAINTING),
                new Category("Blackwork", CategoryType.TATTOO)));

        assertThat(result).extracting(Category::getId).containsExactly(1L, 2L);
        verify(springDataCategoryRepository).saveAll(unsaved);
        verifyNoMoreInteractions(springDataCategoryRepository);
    }
}
