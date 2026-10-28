package com.artseek.application.category.findbytype;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.application.category.CategoryDTO;
import com.artseek.application.category.findbytype.FindCategoriesByTypeQuery;
import com.artseek.application.category.findbytype.FindCategoriesByTypeQueryHandler;
import com.artseek.common.JpaIntegrationTest;
import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import com.artseek.infrastructure.repository.category.CategoryRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({CategoryRepositoryAdapter.class, FindCategoriesByTypeQueryHandler.class})
class FindCategoriesByTypeQueryHandlerIntegrationTests extends JpaIntegrationTest {

    @Autowired
    private FindCategoriesByTypeQueryHandler handler;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void returnsPersistedIdentifiersAndOnlyCategoriesOfTheRequestedType() {
        Category first = categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));
        Category second = categoryRepository.save(new Category("Impressionism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        assertThat(handler.handle(new FindCategoriesByTypeQuery(" PAINTING ")))
                .containsExactlyInAnyOrder(
                        new CategoryDTO(first.getId(), "Surrealism", "painting"),
                        new CategoryDTO(second.getId(), "Impressionism", "painting"));
    }

    @Test
    void returnsEmptyWhenOnlyOtherTypesExist() {
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        assertThat(handler.handle(new FindCategoriesByTypeQuery("painting"))).isEmpty();
    }
}
