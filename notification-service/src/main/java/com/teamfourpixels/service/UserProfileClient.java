package com.teamfourpixels.service;

import com.teamfourpixels.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileClient {
    private final WebClient.Builder webClientBuilder;

    @Cacheable(value = "userSettings", key = "#userId")
    public UserProfileDto fetchUserSettings(Long userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://auth-service/api/internal/profile/{userId}", userId)
                    .retrieve()
                    .bodyToMono(UserProfileDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка связи с auth-service для пользователя {}: {}", userId, e.getMessage());
            return new UserProfileDto(userId, null, null, null, true, false);
        }
    }
}
