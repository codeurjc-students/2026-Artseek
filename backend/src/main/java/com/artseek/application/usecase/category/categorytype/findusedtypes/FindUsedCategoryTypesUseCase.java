package com.artseek.application.usecase.category.categorytype.findusedtypes;

import java.util.List;

import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;

public interface FindUsedCategoryTypesUseCase {

    List<CategoryTypeDTO> execute();
}
