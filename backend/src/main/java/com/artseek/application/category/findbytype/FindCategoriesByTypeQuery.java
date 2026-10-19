package com.artseek.application.category.findbytype;

import com.artseek.application.category.CategoryDTO;
import com.artseek.application.mediator.Query;

import java.util.List;

public record FindCategoriesByTypeQuery(String type) implements Query<List<CategoryDTO>> {
}
