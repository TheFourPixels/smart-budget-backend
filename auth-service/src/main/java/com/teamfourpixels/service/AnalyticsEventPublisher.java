package com.teamfourpixels.service;

import com.teamfourpixels.mapper.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AnalyticsMapper analyticsMapper;

    private static final String ANALYTICS_TOPIC = "analytics-events";

    public void sendEvent(Long userId, String eventType, String payload) {
        try {
            var event = analyticsMapper.toAnalyticsEventDto(userId, eventType, payload);
            kafkaTemplate.send(ANALYTICS_TOPIC, userId.toString(), event);
            log.info("Аналитическое событие {} отправлено для пользователя {}", eventType, userId);
        } catch (Exception e) {
            log.error("Ошибка при отправке аналитического события: {}", e.getMessage());
        }
    }
}
