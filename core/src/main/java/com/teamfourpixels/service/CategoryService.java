package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;
import org.springframework.data.domain.Page;

public interface CategoryService extends CategoryQueryService {
    CategoryDto createCategory(Long userId, CreateCategoryRequest request);
    Page<CategoryDto> getAllCategories(Long userId, int page, int size);
    CategoryDto updateCategory(Long userId, Long id, CreateCategoryRequest request);
    void deleteCategory(Long userId, Long id);
}
