package com.teamfourpixels.service;

import com.teamfourpixels.dto.BudgetLimitEvent;
import com.teamfourpixels.dto.UserProfileDto;
import com.teamfourpixels.entity.DeviceToken;
import com.teamfourpixels.entity.Notification;
import com.teamfourpixels.repository.DeviceTokenRepository;
import com.teamfourpixels.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationRepository notificationRepository;
    private final FirebasePushService firebasePushService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final WebClient.Builder webClientBuilder;

    @KafkaListener(topics = "budget-limit-events", groupId = "notification-group")
    public void handleBudgetLimitEvent(BudgetLimitEvent event) {
        UserProfileDto settings = fetchUserSettings(event.getUserId());

        String title;
        String messageText;
        BigDecimal remaining = event.getLimitAmount().subtract(event.getCurrentSpent());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        if (event.getPercentage() >= 100) {
            title = "Лимит исчерпан!";
            messageText = String.format("Лимит по категории %s исчерпан! Дальнейшие расходы будут сверх плана.",
                    event.getCategoryName());
        } else {
            title = "Внимание!";
            messageText = String.format("Вы потратили %d%% лимита на %s. Осталось %s ₽.",
                    event.getPercentage(),
                    event.getCategoryName(),
                    remaining.setScale(0, RoundingMode.HALF_UP));
        }

        notificationRepository.save(Notification.builder()
                .userId(event.getUserId())
                .title(title)
                .message(messageText)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build());

        if (settings != null && settings.isPushEnabled()) {
            List<DeviceToken> userDevices = deviceTokenRepository.findAllByUserId(event.getUserId());
            for (DeviceToken device : userDevices) {
                firebasePushService.sendPushNotification(device.getToken(), title, messageText);
            }
        }
    }

    private UserProfileDto fetchUserSettings(Long userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://auth-service/api/v1/profile/settings/{userId}", userId)
                    .retrieve()
                    .bodyToMono(UserProfileDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Ошибка связи с auth-service для пользователя {}: {}", userId, e.getMessage());
            return new UserProfileDto(userId, null, null, null, true, false);
        }
    }
}