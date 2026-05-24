package com.teamfourpixels.service;
import com.teamfourpixels.dto.CategoryDto;

public interface CategoryQueryService {
    CategoryDto getCategoryById(Long categoryId);
    java.util.List<CategoryDto> getCategoriesByIds(java.util.List<Long> categoryIds);
}