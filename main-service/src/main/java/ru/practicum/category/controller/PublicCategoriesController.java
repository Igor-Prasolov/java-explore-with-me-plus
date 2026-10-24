package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.page.OffsetPageRequest;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicCategoriesController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @RequestParam(value = "from", required = false, defaultValue = "0") int from,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        Pageable pageable = new OffsetPageRequest(from, size);
        return categoryService.getCategories(pageable);
    }

    @GetMapping("/categories/{categoryId}")
    public CategoryDto getCategoryById(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(value = "from", required = false, defaultValue = "0") int from,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        Pageable pageable = new OffsetPageRequest(from, size);
        return categoryService.getCategoryById(categoryId, pageable);
    }
}
