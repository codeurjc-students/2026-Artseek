package com.artseek.infrastructure.api;

import com.artseek.application.category.CategoryDTO;
import com.artseek.application.category.categorytype.CategoryTypeDTO;
import com.artseek.application.category.categorytype.findusedtypes.FindUsedCategoryTypesQuery;
import com.artseek.application.category.findbytype.FindCategoriesByTypeQuery;
import com.artseek.application.mediator.Mediator;
import com.artseek.domain.exceptions.category.InvalidCategoryTypeException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryRestController {

    private final Mediator mediator;

    public CategoryRestController(Mediator mediator) {
        this.mediator = mediator;
    }

    @GetMapping("/types/used")
    public ResponseEntity<List<CategoryTypeDTO>> findUsedCategoryTypes() {
        return ResponseEntity.ok(mediator.send(new FindUsedCategoryTypesQuery()));
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDTO>> findCategoriesByType(@PathVariable String type) {
        try {
            return ResponseEntity.ok(mediator.send(new FindCategoriesByTypeQuery(type)));
        } catch (InvalidCategoryTypeException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
