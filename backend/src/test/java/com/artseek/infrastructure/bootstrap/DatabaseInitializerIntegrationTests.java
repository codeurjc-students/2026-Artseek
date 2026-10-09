package com.artseek.infrastructure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DatabaseInitializerIntegrationTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void initializesTheExpectedSampleCategories() {
        DatabaseInitializer databaseInitializer = new DatabaseInitializer(categoryRepository);

        databaseInitializer.init();

        assertThat(categoryRepository.findByNameAndType("Surrealism", CategoryType.PAINTING)).isPresent();
        assertThat(categoryRepository.findByNameAndType("Impressionism", CategoryType.PAINTING)).isPresent();
        assertThat(categoryRepository.findByNameAndType("Realism", CategoryType.PAINTING)).isPresent();
        assertThat(categoryRepository.findByNameAndType("Blackwork", CategoryType.TATTOO)).isPresent();
        assertThat(categoryRepository.findByNameAndType("Watercolor", CategoryType.TATTOO)).isPresent();
    }
}
