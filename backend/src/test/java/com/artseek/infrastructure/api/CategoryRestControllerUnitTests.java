package com.artseek.infrastructure.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.artseek.application.usecase.category.CategoryDTO;
import com.artseek.application.usecase.category.categorytype.CategoryTypeDTO;
import com.artseek.application.usecase.category.categorytype.findusedtypes.FindUsedCategoryTypesUseCase;
import com.artseek.application.usecase.category.findbytype.FindCategoriesByTypeUseCase;
import com.artseek.domain.exceptions.category.InvalidCategoryTypeException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryRestController.class)
class CategoryRestControllerUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindUsedCategoryTypesUseCase findUsedCategoryTypes;

    @MockitoBean
    private FindCategoriesByTypeUseCase findCategoriesByType;

    @Test
    void returnsUsedCategoryTypes() throws Exception {
        when(findUsedCategoryTypes.execute()).thenReturn(List.of(
                new CategoryTypeDTO("painting"),
                new CategoryTypeDTO("tattoo")));

        mockMvc.perform(get("/api/categories/types/used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("painting"))
                .andExpect(jsonPath("$[1].value").value("tattoo"));

        verify(findUsedCategoryTypes).execute();
        verifyNoMoreInteractions(findUsedCategoryTypes, findCategoriesByType);
    }

    @Test
    void returnsCategoriesFilteredByType() throws Exception {
        when(findCategoriesByType.execute("painting")).thenReturn(List.of(
                new CategoryDTO(1L, "Surrealism", "painting"),
                new CategoryDTO(2L, "Impressionism", "painting")));

        mockMvc.perform(get("/api/categories/painting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Surrealism"))
                .andExpect(jsonPath("$[0].type").value("painting"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Impressionism"))
                .andExpect(jsonPath("$[1].type").value("painting"));

        verify(findCategoriesByType).execute("painting");
        verifyNoMoreInteractions(findUsedCategoryTypes, findCategoriesByType);
    }

    @Test
    void returnsBadRequestForAnInvalidCategoryType() throws Exception {
        when(findCategoriesByType.execute("sculpture"))
                .thenThrow(new InvalidCategoryTypeException("sculpture"));

        mockMvc.perform(get("/api/categories/sculpture"))
                .andExpect(status().isBadRequest());

        verify(findCategoriesByType).execute("sculpture");
        verifyNoMoreInteractions(findUsedCategoryTypes, findCategoriesByType);
    }
}
