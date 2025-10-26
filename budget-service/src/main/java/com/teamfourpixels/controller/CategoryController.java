package com.teamfourpixels.controller;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;
import com.teamfourpixels.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private static final Long DUMMY_USER_ID = 1L;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories(DUMMY_USER_ID));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CreateCategoryRequest request) {
        return ResponseEntity.created(null).body(categoryService.createCategory(DUMMY_USER_ID, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
                                                      @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(DUMMY_USER_ID, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(DUMMY_USER_ID, id);
        return ResponseEntity.noContent().build();
    }
}