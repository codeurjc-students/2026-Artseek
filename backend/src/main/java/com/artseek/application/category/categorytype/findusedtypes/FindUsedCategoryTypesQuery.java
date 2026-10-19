package com.artseek.application.category.categorytype.findusedtypes;

import com.artseek.application.category.categorytype.CategoryTypeDTO;
import com.artseek.application.mediator.Query;

import java.util.List;

public record FindUsedCategoryTypesQuery() implements Query<List<CategoryTypeDTO>> {
}
