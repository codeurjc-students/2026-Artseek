package com.artseek;

import static org.assertj.core.api.Assertions.assertThat;

import com.artseek.common.FullContextIntegrationTest;
import com.artseek.domain.repository.CategoryRepository;
import com.artseek.infrastructure.bootstrap.DatabaseInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

class ArtseekApplicationIntegrationTests extends FullContextIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void connectsToAnIsolatedPostgreSqlDatabase() {
        assertThat(jdbcTemplate.queryForObject("select current_database()", String.class))
                .isEqualTo("artseek_test");
        assertThat(jdbcTemplate.queryForObject("select version()", String.class))
                .startsWith("PostgreSQL 17.11 ");
    }

    @Test
    void doesNotActivateTheLocalProfileOrLoadSampleData() {
        assertThat(applicationContext.getEnvironment().getActiveProfiles()).doesNotContain("local");
        assertThat(applicationContext.getBeansOfType(DatabaseInitializer.class)).isEmpty();
        assertThat(categoryRepository.findUsedTypes()).isEmpty();
    }
}
