package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;
import com.teamfourpixels.entity.Category;
import com.teamfourpixels.mapper.CategoryMapper;
import com.teamfourpixels.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(Long userId, CreateCategoryRequest request) {
        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .isSystem(false)
                .build();
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public Page<CategoryDto> getAllCategories(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findByUserId(userId, pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long userId, Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));

        if (!userId.equals(category.getUserId()) || category.isSystem()) {
            throw new IllegalArgumentException("Нельзя редактировать системную или чужую категорию");
        }

        category.setName(request.getName());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));

        if (!userId.equals(category.getUserId()) || category.isSystem()) {
            throw new IllegalArgumentException("Нельзя удалять системные или чужие категории");
        }
        categoryRepository.deleteById(id);
    }
}