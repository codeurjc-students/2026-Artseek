package com.artseek.application.usecase.category.findbytype;

import com.artseek.application.usecase.category.CategoryDTO;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindCategoriesByTypeService implements FindCategoriesByTypeUseCase {

    private final CategoryRepository categoryRepository;

    public FindCategoriesByTypeService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> execute(String type) {
        CategoryType categoryType = CategoryType.fromValue(type);

        return categoryRepository.findByType(categoryType).stream()
                .map(CategoryDTO::from)
                .toList();
    }
}
