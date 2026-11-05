package com.artseek.domain.category.repository;

import java.util.List;
import java.util.Optional;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;

public interface CategoryRepository {

    Optional<Category> findById(Long id);

    List<Category> findByType(CategoryType type);

    List<CategoryType> findUsedTypes();

    boolean existsByNameAndType(String name, CategoryType type);

    Category save(Category category);

    List<Category> saveAll(List<Category> categories);
}
