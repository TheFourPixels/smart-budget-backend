package com.teamfourpixels.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.teamfourpixels.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebasePushService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void sendPushNotification(String targetToken, String title, String body) {
        if (targetToken == null || targetToken.isBlank()) return;

        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if ("registration-token-not-registered".equals(e.getMessagingErrorCode().name().toLowerCase())
                    || "invalid-argument".equals(e.getMessagingErrorCode().name().toLowerCase())) {
                log.warn("Удаление невалидного токена: {}", targetToken);
                deviceTokenRepository.deleteByToken(targetToken);
            } else {
                log.error("Ошибка Firebase: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Критическая ошибка отправки: {}", e.getMessage());
        }
    }
}