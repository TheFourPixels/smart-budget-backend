package com.teamfourpixels.service;
import com.teamfourpixels.dto.CategoryDto;

public interface CategoryQueryService {
    CategoryDto getCategoryById(Long categoryId);
}