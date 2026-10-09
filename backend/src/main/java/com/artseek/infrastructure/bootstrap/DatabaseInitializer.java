package com.artseek.infrastructure.bootstrap;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;

import jakarta.annotation.PostConstruct;

@Service
@Profile("local")
public class DatabaseInitializer {

    private final CategoryRepository categoryRepository;

    public DatabaseInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void init() {
        List<Category> missingCategories = List.of(
                        new Category("Surrealism", CategoryType.PAINTING),
                        new Category("Impressionism", CategoryType.PAINTING),
                        new Category("Realism", CategoryType.PAINTING),
                        new Category("Blackwork", CategoryType.TATTOO),
                        new Category("Watercolor", CategoryType.TATTOO))
                .stream()
                .filter(category -> categoryRepository.findByNameAndType(
                        category.getName(), category.getType()).isEmpty())
                .toList();

        if (!missingCategories.isEmpty()) {
            categoryRepository.saveAll(missingCategories);
        }
    }
}
