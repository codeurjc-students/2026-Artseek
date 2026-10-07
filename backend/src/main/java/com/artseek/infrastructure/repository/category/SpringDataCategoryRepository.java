package com.artseek.infrastructure.repository.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;

public interface SpringDataCategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(CategoryType type);

    Optional<Category> findByNameAndType(String name, CategoryType type);
}
