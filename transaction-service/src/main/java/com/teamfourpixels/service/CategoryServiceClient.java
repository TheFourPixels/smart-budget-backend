package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDto;
import com.teamfourpixels.dto.CreateCategoryRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collections;

@Service
@Primary
public class CategoryServiceClient implements CategoryService {

    private final WebClient webClient;

    public CategoryServiceClient(@Qualifier("budgetWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new EntityNotFoundException("Category ID cannot be null or zero.");
        }

        try {
            return webClient.get()
                    .uri("/categories/{id}", categoryId)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
                        throw new EntityNotFoundException("Категория не найдена с ID: " + categoryId);
                    })
                    .bodyToMono(CategoryDto.class)
                    .block();

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось связаться с Budget Service или произошла ошибка: " + e.getMessage(), e);
        }
    }

    @Override
    public CategoryDto createCategory(Long userId, CreateCategoryRequest request) {
        throw new UnsupportedOperationException("Creating categories is managed by Budget Service.");
    }

    @Override
    public Page<CategoryDto> getAllCategories(Long userId, int page, int size) {
        return new PageImpl<>(Collections.emptyList());
    }

    @Override
    public CategoryDto updateCategory(Long userId, Long id, CreateCategoryRequest request) {
        throw new UnsupportedOperationException("Updating categories is managed by Budget Service.");
    }

    @Override
    public void deleteCategory(Long userId, Long id) {
        throw new UnsupportedOperationException("Deleting categories is managed by Budget Service.");
    }
}