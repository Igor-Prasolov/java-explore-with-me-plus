package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.entity.Category;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidateException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        log.info("Creating new category {}", category);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        log.info("Retrieving categories with pagination: page number = {}, page size = {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return categoryPage.map(categoryMapper::toCategoryDto).getContent();
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId, Pageable pageable) {
        log.info("Retrieving category with id = {}", categoryId);

        Optional<Category> category = categoryRepository.findById(categoryId);

        return categoryMapper.toCategoryDto(category.orElseThrow(() ->
                NotFoundException.of(Category.class, categoryId)
        ));
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryById(Long categoryId, NewCategoryDto newCategoryDto) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> NotFoundException.of(Category.class, categoryId));
        log.info("Updating category with id = {}", categoryId);

        existingCategory.setName(newCategoryDto.getName());

        return categoryMapper.toCategoryDto(categoryRepository.save(existingCategory));
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            throw NotFoundException.of(Category.class, categoryId);
        }

        if (eventRepository.existsByCategoryId(categoryId)) {
            throw ValidateException.of(ErrorMessages.CATEGORY_NOT_EMPTY);
        }

        categoryRepository.deleteById(categoryId);
    }

}
