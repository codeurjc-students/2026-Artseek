package com.artseek.infrastructure.api;

import com.artseek.application.usecase.category.CategoryDTO;
import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;
import com.artseek.application.usecase.category.categorytype.findusedtypes.FindUsedCategoryTypesUseCase;
import com.artseek.application.usecase.category.findbytype.FindCategoriesByTypeUseCase;
import com.artseek.domain.exceptions.category.InvalidCategoryTypeException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final FindUsedCategoryTypesUseCase findUsedCategoryTypes;
    private final FindCategoriesByTypeUseCase findCategoriesByType;

    public CategoryRestController(
            FindUsedCategoryTypesUseCase findUsedCategoryTypes,
            FindCategoriesByTypeUseCase findCategoriesByType) {
        this.findUsedCategoryTypes = findUsedCategoryTypes;
        this.findCategoriesByType = findCategoriesByType;
    }

    @GetMapping("/types/used")
    public ResponseEntity<List<CategoryTypeDTO>> findUsedCategoryTypes() {
        return ResponseEntity.ok(findUsedCategoryTypes.execute());
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDTO>> findCategoriesByType(@PathVariable String type) {
        try {
            return ResponseEntity.ok(findCategoriesByType.execute(type));
        } catch (InvalidCategoryTypeException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
