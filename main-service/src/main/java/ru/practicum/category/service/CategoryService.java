package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long categoryId, Integer from, Integer size);

    void deleteCategoryById(Long categoryId);

    CategoryDto updateCategoryById(Long categoryId, NewCategoryDto newCategoryDto);
}
