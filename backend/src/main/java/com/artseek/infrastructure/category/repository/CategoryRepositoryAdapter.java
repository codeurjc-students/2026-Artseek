package com.artseek.infrastructure.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;
import com.artseek.domain.category.repository.CategoryRepository;

@Repository 
public class CategoryRepositoryAdapter implements CategoryRepository {
    
    private final SpringDataCategoryRepository springCategoryRepository;  
    
    public CategoryRepositoryAdapter(SpringDataCategoryRepository springCategoryRepository) {
        this.springCategoryRepository = springCategoryRepository;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return springCategoryRepository.findById(id);
    }

    @Override
    public List<Category> findByType(CategoryType type) {
        return springCategoryRepository.findByType(type);
    }

    @Override
    public List<CategoryType> findUsedTypes() {
        return springCategoryRepository.findUsedTypes();
    }

    @Override
    public boolean existsByNameAndType(String name, CategoryType type) {
        return springCategoryRepository.existsByNameAndType(name, type);
    }

    @Override
    public Category save(Category category) {
        return springCategoryRepository.save(category);
    }

    @Override
    public List<Category> saveAll(List<Category> categories) {
        return springCategoryRepository.saveAll(categories);
    }
}
