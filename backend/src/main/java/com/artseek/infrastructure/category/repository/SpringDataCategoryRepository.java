package com.artseek.infrastructure.category.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

interface SpringDataCategoryRepository extends ListCrudRepository<CategoryPersistenceModel, Long> {

    List<CategoryPersistenceModel> findByType(String type);

    boolean existsByNameAndType(String name, String type);

    @Query("SELECT DISTINCT type FROM categories ORDER BY type")
    List<String> findUsedTypes();
}
