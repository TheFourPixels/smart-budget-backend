package com.teamfourpixels.controller;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;
import com.teamfourpixels.security.UserPrincipal;
import com.teamfourpixels.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(categoryService.getAllCategories(userPrincipal.getId()));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                      @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.created(null).body(categoryService.createCategory(userPrincipal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                      @PathVariable Long id,
                                                      @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(userPrincipal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable Long id) {
        categoryService.deleteCategory(userPrincipal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}