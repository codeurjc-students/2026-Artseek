package com.artseek.application.usecase.category.categorytype.findusedtypes;

import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;
import com.artseek.domain.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindUsedCategoryTypesService implements FindUsedCategoryTypesUseCase {

    private final CategoryRepository categoryRepository;

    public FindUsedCategoryTypesService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTypeDTO> execute() {
        return categoryRepository.findUsedTypes().stream()
                .map(CategoryTypeDTO::from)
                .toList();
    }
}
