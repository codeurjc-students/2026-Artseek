package com.artseek.infrastructure.repository.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@Import(CategoryRepositoryAdapter.class)
class CategoryRepositoryAdapterIntegrationTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesACategoryAndAssignsItsIdentifier() {
        Category category = new Category("Surrealism", CategoryType.PAINTING);

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull().isPositive();
        assertThat(savedCategory.getName()).isEqualTo("Surrealism");
        assertThat(savedCategory.getType()).isEqualTo(CategoryType.PAINTING);
    }

    @Test
    void findsACategoryById() {
        Category savedCategory = categoryRepository.save(
                new Category("Surrealism", CategoryType.PAINTING));

        assertThat(categoryRepository.findById(savedCategory.getId()))
                .isPresent()
                .get()
                .satisfies(category -> {
                    assertThat(category.getName()).isEqualTo("Surrealism");
                    assertThat(category.getType()).isEqualTo(CategoryType.PAINTING);
                });
    }

    @Test
    void persistsTheCategoryTypeByNameInsteadOfOrdinal() {
        Category savedCategory = categoryRepository.save(
                new Category("Surrealism", CategoryType.PAINTING));

        String storedType = jdbcTemplate.queryForObject(
                "select type from categories where id = ?",
                String.class,
                savedCategory.getId());

        assertThat(storedType).isEqualTo("PAINTING");
    }

    @Test
    void findsCategoriesByType() {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Impressionism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        assertThat(categoryRepository.findByType(CategoryType.PAINTING))
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Surrealism", "Impressionism");
    }

    @Test
    void findsUsedCategoryTypesWithoutDuplicates() {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Impressionism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        assertThat(categoryRepository.findUsedTypes())
                .containsExactly(CategoryType.PAINTING, CategoryType.TATTOO);
    }

    @Test
    void findsACategoryByNameAndType() {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));

        assertThat(categoryRepository.findByNameAndType("Surrealism", CategoryType.PAINTING))
                .isPresent()
                .get()
                .extracting(Category::getName)
                .isEqualTo("Surrealism");
    }

    @Test
    void returnsEmptyWhenNameAndTypeDoNotMatch() {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));

        assertThat(categoryRepository.findByNameAndType("Cubism", CategoryType.PAINTING))
                .isEmpty();
    }
}
