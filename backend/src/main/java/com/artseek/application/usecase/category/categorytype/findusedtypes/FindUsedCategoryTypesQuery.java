package com.artseek.application.usecase.category.categorytype.findusedtypes;

import com.artseek.application.mediator.Query;
import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;
import java.util.List;

public record FindUsedCategoryTypesQuery() implements Query<List<CategoryTypeDTO>> {
}
