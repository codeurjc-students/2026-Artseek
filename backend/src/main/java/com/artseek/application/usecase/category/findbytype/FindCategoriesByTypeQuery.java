package com.artseek.application.usecase.category.findbytype;

import com.artseek.application.mediator.Query;
import com.artseek.application.usecase.category.CategoryDTO;
import java.util.List;

public record FindCategoriesByTypeQuery(String type) implements Query<List<CategoryDTO>> {
}
