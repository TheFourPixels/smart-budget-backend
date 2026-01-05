package com.teamfourpixels.controller;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;
import com.teamfourpixels.service.CategoryService;
import com.teamfourpixels.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Page<CategoryDto> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return categoryService.getAllCategories(UserContext.getUserId(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(UserContext.getUserId(), request);
    }

    @PutMapping("/{id}")
    public CategoryDto updateCategory(@PathVariable Long id,
                                      @Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.updateCategory(UserContext.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(UserContext.getUserId(), id);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }
}