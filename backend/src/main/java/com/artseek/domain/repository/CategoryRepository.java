package com.artseek.domain.repository;

import java.util.List;
import java.util.Optional;

import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;

public interface CategoryRepository {

    Optional<Category> findById(Long id);

    List<Category> findByType(CategoryType type);

    List<CategoryType> findUsedTypes();

    Optional<Category> findByNameAndType(String name, CategoryType type);

    Category save(Category category);


}
