package com.artseek.infrastructure.category.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;
import com.artseek.domain.category.repository.CategoryRepository;
import com.artseek.shared.JdbcIntegrationTest;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(CategoryRepositoryAdapter.class)
class CategoryRepositoryAdapterIntegrationTests extends JdbcIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void usesAnIsolatedPostgreSqlDatabase() {
        assertThat(jdbcTemplate.queryForObject("select current_database()", String.class))
                .isEqualTo("artseek_test");
        assertThat(jdbcTemplate.queryForObject("select version()", String.class))
                .startsWith("PostgreSQL 17.11 ");
    }

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
    void reportsWhetherACategoryExistsByNameAndType() {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));

        assertThat(categoryRepository.existsByNameAndType("Surrealism", CategoryType.PAINTING)).isTrue();
        assertThat(categoryRepository.existsByNameAndType("Cubism", CategoryType.PAINTING)).isFalse();
        assertThat(categoryRepository.existsByNameAndType("Surrealism", CategoryType.TATTOO)).isFalse();
    }

    @Test
    void savesAllCategoriesAndReturnsThemWithIdentifiers() {
        List<Category> savedCategories = categoryRepository.saveAll(List.of(
                new Category("Surrealism", CategoryType.PAINTING),
                new Category("Blackwork", CategoryType.TATTOO)));

        assertThat(savedCategories)
                .extracting(Category::getId)
                .doesNotContainNull()
                .doesNotHaveDuplicates();
        assertThat(savedCategories)
                .extracting(Category::getName)
                .containsExactly("Surrealism", "Blackwork");
    }
}
