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
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

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
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id = " + categoryId + " not found"));
        log.info("Retrieving category with id = {}", categoryId);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategoryById(Long categoryId, NewCategoryDto newCategoryDto) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id = " + categoryId + " not found"));
        log.info("Updating category with id = {}", categoryId);

        if (newCategoryDto.getName() != null && !newCategoryDto.getName().isBlank()) {
            existingCategory.setName(newCategoryDto.getName());
        }

        return categoryMapper.toCategoryDto(categoryRepository.save(existingCategory));
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) {


        // Нужна проверка на события, которые ссылаются на эту категорию, дописать после создания EventService

        log.info("Deleting category with id = {}", categoryId);
        Integer deletedCount = categoryRepository.deleteCategoryById(categoryId);
        if (deletedCount == 0) {
            throw new NotFoundException("Category with id = " + categoryId + " not found");
        }
    }

}
