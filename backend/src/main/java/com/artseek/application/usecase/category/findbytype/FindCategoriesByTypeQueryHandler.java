package com.artseek.application.usecase.category.findbytype;

import com.artseek.application.mediator.QueryHandler;
import com.artseek.application.usecase.category.CategoryDTO;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindCategoriesByTypeQueryHandler
        implements QueryHandler<FindCategoriesByTypeQuery, List<CategoryDTO>> {

    private final CategoryRepository categoryRepository;

    public FindCategoriesByTypeQueryHandler(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Class<FindCategoriesByTypeQuery> queryType() {
        return FindCategoriesByTypeQuery.class;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> handle(FindCategoriesByTypeQuery query) {
        CategoryType categoryType = CategoryType.fromValue(query.type());

        return categoryRepository.findByType(categoryType).stream()
                .map(CategoryDTO::from)
                .toList();
    }
}
