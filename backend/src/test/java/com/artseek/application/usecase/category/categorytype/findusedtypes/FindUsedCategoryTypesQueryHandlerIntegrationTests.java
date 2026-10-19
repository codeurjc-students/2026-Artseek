package com.artseek.application.usecase.category.categorytype.findusedtypes;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;
import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import com.artseek.infrastructure.repository.category.CategoryRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({CategoryRepositoryAdapter.class, FindUsedCategoryTypesQueryHandler.class})
class FindUsedCategoryTypesQueryHandlerIntegrationTests {

    @Autowired
    private FindUsedCategoryTypesQueryHandler handler;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void returnsDistinctUsedTypesInRepositoryOrder() {
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Impressionism", CategoryType.PAINTING));

        assertThat(handler.handle(new FindUsedCategoryTypesQuery()))
                .containsExactly(new CategoryTypeDTO("painting"), new CategoryTypeDTO("tattoo"));
    }

    @Test
    void excludesSupportedTypesThatHaveNoCategories() {
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        assertThat(handler.handle(new FindUsedCategoryTypesQuery()))
                .containsExactly(new CategoryTypeDTO("tattoo"));
    }

    @Test
    void returnsEmptyWhenNoCategoriesExist() {
        assertThat(handler.handle(new FindUsedCategoryTypesQuery())).isEmpty();
    }
}
