package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(Long userId, CreateCategoryRequest request);
    List<CategoryDto> getAllCategories(Long userId);
    CategoryDto updateCategory(Long userId, Long id, CreateCategoryRequest request);
    void deleteCategory(Long userId, Long id);
}
