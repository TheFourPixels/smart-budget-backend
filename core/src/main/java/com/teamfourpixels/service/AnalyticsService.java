package com.teamfourpixels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfourpixels.mapper.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AnalyticsMapper analyticsMapper;
    private final ObjectMapper objectMapper;

    @Async
    public void sendEvent(Long userId, String eventType, Object payload) {
        try {
            String jsonPayload;
            if (payload instanceof String s) {
                jsonPayload = s;
            } else {
                jsonPayload = objectMapper.writeValueAsString(payload);
            }
            var event = analyticsMapper.toAnalyticsEventDto(userId, eventType, jsonPayload);
            kafkaTemplate.send("analytics-events", userId.toString(), event);
            log.info("Аналитика [{}]: отправлено для пользователя {}", eventType, userId);
        } catch (Exception e) {
            log.error("Ошибка аналитики: {}", e.getMessage());
        }
    }
}
