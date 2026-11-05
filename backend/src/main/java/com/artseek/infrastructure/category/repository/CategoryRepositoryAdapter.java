package com.artseek.infrastructure.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.artseek.domain.category.Category;
import com.artseek.domain.category.CategoryType;
import com.artseek.domain.category.repository.CategoryRepository;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final SpringDataCategoryRepository springDataCategoryRepository;

    public CategoryRepositoryAdapter(SpringDataCategoryRepository springDataCategoryRepository) {
        this.springDataCategoryRepository = springDataCategoryRepository;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return springDataCategoryRepository.findById(id).map(CategoryMapper::toDomain);
    }

    @Override
    public List<Category> findByType(CategoryType type) {
        return springDataCategoryRepository.findByType(type.name()).stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<CategoryType> findUsedTypes() {
        return springDataCategoryRepository.findUsedTypes().stream()
                .map(CategoryType::valueOf)
                .toList();
    }

    @Override
    public boolean existsByNameAndType(String name, CategoryType type) {
        return springDataCategoryRepository.existsByNameAndType(name, type.name());
    }

    @Override
    public Category save(Category category) {
        CategoryPersistenceModel saved = springDataCategoryRepository.save(CategoryMapper.toPersistence(category));
        return CategoryMapper.toDomain(saved);
    }

    @Override
    public List<Category> saveAll(List<Category> categories) {
        List<CategoryPersistenceModel> models = categories.stream()
                .map(CategoryMapper::toPersistence)
                .toList();
        return springDataCategoryRepository.saveAll(models).stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }
}
