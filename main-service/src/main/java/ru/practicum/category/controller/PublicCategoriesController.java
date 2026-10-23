package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicCategoriesController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/categories/{categoryId}")
    public CategoryDto getCategoryById(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return categoryService.getCategoryById(categoryId, from, size);
    }
}
