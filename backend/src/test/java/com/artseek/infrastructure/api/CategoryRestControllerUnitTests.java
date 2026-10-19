package com.artseek.infrastructure.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.artseek.application.category.CategoryDTO;
import com.artseek.application.category.categorytype.CategoryTypeDTO;
import com.artseek.application.category.categorytype.findusedtypes.FindUsedCategoryTypesQuery;
import com.artseek.application.category.findbytype.FindCategoriesByTypeQuery;
import com.artseek.application.mediator.Mediator;
import com.artseek.domain.exceptions.category.InvalidCategoryTypeException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryRestController.class)
class CategoryRestControllerUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Mediator mediator;

    @Test
    void returnsUsedCategoryTypes() throws Exception {
        FindUsedCategoryTypesQuery query = new FindUsedCategoryTypesQuery();
        when(mediator.send(query)).thenReturn(List.of(
                new CategoryTypeDTO("painting"),
                new CategoryTypeDTO("tattoo")));

        mockMvc.perform(get("/api/1.0/categories/types/used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("painting"))
                .andExpect(jsonPath("$[1].value").value("tattoo"));

        verify(mediator).send(query);
        verifyNoMoreInteractions(mediator);
    }

    @Test
    void returnsCategoriesFilteredByType() throws Exception {
        FindCategoriesByTypeQuery query = new FindCategoriesByTypeQuery("painting");
        when(mediator.send(query)).thenReturn(List.of(
                new CategoryDTO(1L, "Surrealism", "painting"),
                new CategoryDTO(2L, "Impressionism", "painting")));

        mockMvc.perform(get("/api/1.0/categories/painting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Surrealism"))
                .andExpect(jsonPath("$[0].type").value("painting"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Impressionism"))
                .andExpect(jsonPath("$[1].type").value("painting"));

        verify(mediator).send(query);
        verifyNoMoreInteractions(mediator);
    }

    @Test
    void returnsBadRequestForAnInvalidCategoryType() throws Exception {
        FindCategoriesByTypeQuery query = new FindCategoriesByTypeQuery("sculpture");
        when(mediator.send(query))
                .thenThrow(new InvalidCategoryTypeException("sculpture"));

        mockMvc.perform(get("/api/1.0/categories/sculpture"))
                .andExpect(status().isBadRequest());

        verify(mediator).send(query);
        verifyNoMoreInteractions(mediator);
    }
}
