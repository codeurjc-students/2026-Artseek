package com.artseek.infrastructure.repository.category;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;

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
    public Optional<Category> findByNameAndType(String name, CategoryType type) {
        return springCategoryRepository.findByNameAndType(name, type);
    }

    @Override
    public Category save(Category category) {
        return springCategoryRepository.save(category);
    }


}
