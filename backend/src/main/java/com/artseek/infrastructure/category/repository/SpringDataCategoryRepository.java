package com.artseek.infrastructure.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;

public interface SpringDataCategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(CategoryType type);

    @Query("select distinct category.type from Category category order by category.type")
    List<CategoryType> findUsedTypes();

    boolean existsByNameAndType(String name, CategoryType type);
}
