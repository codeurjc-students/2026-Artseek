package com.artseek.application.usecase.category.findbytype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.artseek.application.usecase.category.CategoryDTO;
import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import com.artseek.infrastructure.repository.category.CategoryRepositoryAdapter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({CategoryRepositoryAdapter.class, FindCategoriesByTypeService.class})
class FindCategoriesByTypeServiceIntegrationTests {

    @Autowired
    private FindCategoriesByTypeUseCase useCase;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void returnsOnlyPersistedCategoriesOfTheRequestedTypeAsDTOs() {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Impressionism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        List<CategoryDTO> result = useCase.execute("painting");

        assertThat(result)
                .allSatisfy(category -> assertThat(category.id()).isPositive())
                .extracting(CategoryDTO::name, CategoryDTO::type)
                .containsExactly(
                        tuple("Surrealism", "painting"),
                        tuple("Impressionism", "painting"));
    }
}
