package com.artseek.infrastructure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.domain.category.CategoryType;
import com.artseek.domain.category.repository.CategoryRepository;
import com.artseek.shared.FullContextIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class DatabaseInitializerIntegrationTests extends FullContextIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void initializesTheExpectedSampleCategories() {
        DatabaseInitializer databaseInitializer = new DatabaseInitializer(categoryRepository);

        databaseInitializer.init();

        assertThat(categoryRepository.existsByNameAndType("Surrealism", CategoryType.PAINTING)).isTrue();
        assertThat(categoryRepository.existsByNameAndType("Impressionism", CategoryType.PAINTING)).isTrue();
        assertThat(categoryRepository.existsByNameAndType("Realism", CategoryType.PAINTING)).isTrue();
        assertThat(categoryRepository.existsByNameAndType("Blackwork", CategoryType.TATTOO)).isTrue();
        assertThat(categoryRepository.existsByNameAndType("Watercolor", CategoryType.TATTOO)).isTrue();
    }

    @Test
    void doesNotDuplicateSampleCategoriesWhenRunAgain() {
        DatabaseInitializer databaseInitializer = new DatabaseInitializer(categoryRepository);

        databaseInitializer.init();
        databaseInitializer.init();

        assertThat(jdbcTemplate.queryForObject("select count(*) from categories", Integer.class))
                .isEqualTo(5);
    }
}
