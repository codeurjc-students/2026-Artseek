package com.artseek.application.usecase.category.findbytype;

import com.artseek.application.usecase.category.CategoryDTO;
import java.util.List;

public interface FindCategoriesByTypeUseCase {

    List<CategoryDTO> execute(String type);
}
