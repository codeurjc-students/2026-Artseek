package com.artseek.application.usecase.category.categorytype.findusedtypes;

import com.artseek.application.mediator.QueryHandler;
import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;
import com.artseek.domain.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindUsedCategoryTypesQueryHandler
        implements QueryHandler<FindUsedCategoryTypesQuery, List<CategoryTypeDTO>> {

    private final CategoryRepository categoryRepository;

    public FindUsedCategoryTypesQueryHandler(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Class<FindUsedCategoryTypesQuery> queryType() {
        return FindUsedCategoryTypesQuery.class;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTypeDTO> handle(FindUsedCategoryTypesQuery query) {
        return categoryRepository.findUsedTypes().stream()
                .map(CategoryTypeDTO::from)
                .toList();
    }
}
