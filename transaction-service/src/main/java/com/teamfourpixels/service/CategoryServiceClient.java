package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class CategoryServiceClient implements CategoryQueryService {

    private final WebClient budgetWebClient;

    public CategoryServiceClient(@Qualifier("budgetWebClient") WebClient budgetWebClient) {
        this.budgetWebClient = budgetWebClient;
    }

    @Override
    public List<CategoryDto> getCategoriesByIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        String jwt = "";
        try {
            jwt = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        } catch (Exception e) {
        }

        try {
            return budgetWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/categories/list")
                            .queryParam("ids", categoryIds)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .bodyToFlux(CategoryDto.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new EntityNotFoundException("Category ID cannot be null or zero.");
        }

        String jwt = "";
        try {
            jwt = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        } catch (Exception e) {
        }

        try {
            return budgetWebClient.get()
                    .uri("/api/v1/categories/{id}", categoryId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .onStatus(org.springframework.http.HttpStatusCode::is4xxClientError, response -> 
                        Mono.error(new EntityNotFoundException("Категория не найдена с ID: " + categoryId))
                    )
                    .bodyToMono(CategoryDto.class)
                    .block();

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось связаться с Budget Service или произошла ошибка: " + e.getMessage(), e);
        }
    }
}
