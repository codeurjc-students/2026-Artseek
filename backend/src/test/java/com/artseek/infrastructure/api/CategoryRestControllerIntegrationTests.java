package com.artseek.infrastructure.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.artseek.domain.model.category.Category;
import com.artseek.domain.model.category.CategoryType;
import com.artseek.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryRestControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void returnsOnlyCategoryTypesThatAreUsed() throws Exception {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Impressionism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        mockMvc.perform(get("/api/categories/types/used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].value").value("painting"))
                .andExpect(jsonPath("$[1].value").value("tattoo"));
    }

    @Test
    void returnsOnlyCategoriesOfTheRequestedType() throws Exception {
        categoryRepository.save(new Category("Surrealism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Impressionism", CategoryType.PAINTING));
        categoryRepository.save(new Category("Blackwork", CategoryType.TATTOO));

        mockMvc.perform(get("/api/categories/painting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                        "Surrealism", "Impressionism")))
                .andExpect(jsonPath("$[*].type", everyItem(is("painting"))));
    }

    @Test
    void returnsBadRequestForAnInvalidCategoryType() throws Exception {
        mockMvc.perform(get("/api/categories/sculpture"))
                .andExpect(status().isBadRequest());
    }
}
