package ru.practicum.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategories(Pageable pageable);

    CategoryDto getCategoryById(Long categoryId, Pageable pageable);

    void deleteCategoryById(Long categoryId);

    CategoryDto updateCategoryById(Long categoryId, NewCategoryDto newCategoryDto);
}
