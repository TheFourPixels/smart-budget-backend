package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class CategoryServiceClient implements CategoryQueryService {

    private final WebClient budgetWebClient;

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new EntityNotFoundException("Category ID cannot be null or zero.");
        }

        String jwt = "";
        try {
            jwt = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        } catch (Exception e) {
            //если вызываем из async без контекста можно логировать или обрабатывать
        }

        try {
            return budgetWebClient.get()
                    .uri("/api/v1/categories", categoryId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt) // <---
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
}