package ru.practicum.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminCategoriesController.class, PublicCategoriesController.class})
public class CategoryControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mvc;

    private final NewCategoryDto newCategoryDto = new NewCategoryDto("Что то новое");

    private final CategoryDto categoryDto = new CategoryDto(1L, "Что то новое");

    @Test
    void create() throws Exception {
        when(categoryService.createCategory(any(NewCategoryDto.class))).thenReturn(categoryDto);

        mvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(newCategoryDto.getName()), String.class));
    }

    @Test
    void getCategories() throws Exception {
        when(categoryService.getCategories(any(Pageable.class))).thenReturn(List.of(categoryDto));

        mvc.perform(get("/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(categoryDto.getName())));
    }

    @Test
    void getCategoryById() throws Exception {
        when(categoryService.getCategoryById(anyLong(), any(Pageable.class))).thenReturn(categoryDto);

        mvc.perform(get("/categories/{id}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));
    }

    @Test
    void deleteCategoryById() throws Exception {
        mvc.perform(delete("/admin/categories/{id}", "1")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCategoryById() throws Exception {
        when(categoryService.updateCategoryById(anyLong(), any(NewCategoryDto.class)))
                .thenReturn(categoryDto);

        mvc.perform(patch("/admin/categories/{id}", 1)
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(newCategoryDto.getName()), String.class));
    }
}
