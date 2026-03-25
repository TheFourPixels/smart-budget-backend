package com.teamfourpixels.controller;

import com.teamfourpixels.entity.Notification;
import com.teamfourpixels.repository.DeviceTokenRepository;
import com.teamfourpixels.repository.NotificationRepository;
import com.teamfourpixels.entity.DeviceToken;
import com.teamfourpixels.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository repository;

    private final DeviceTokenRepository deviceTokenRepository;

    @GetMapping
    public List<Notification> getMyNotifications(@RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly) {
        Long userId = UserContext.getUserId();

        if (unreadOnly) {
            return repository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        } else {
            return repository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
        }
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();
        repository.findById(id).ifPresent(n -> {
            if (!n.getUserId().equals(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к чужому уведомлению");
            }
            n.setRead(true);
            repository.save(n);
        });
    }

    @PostMapping("/tokens")
    public void registerDeviceToken(@RequestParam String token) {
        Long userId = UserContext.getUserId();

        if (!deviceTokenRepository.existsByUserIdAndToken(userId, token)) {
            DeviceToken deviceToken = DeviceToken.builder()
                    .userId(userId)
                    .token(token)
                    .createdAt(LocalDateTime.now())
                    .build();

            deviceTokenRepository.save(deviceToken);
            log.info("Новый токен устройства сохранен для пользователя ID: {}", userId);
        }
    }
}