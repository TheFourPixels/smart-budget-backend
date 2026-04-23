package com.teamfourpixels.service;

import com.teamfourpixels.dto.AnalyticsEventDto;
import com.teamfourpixels.mapper.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedAnalyticsService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AnalyticsMapper analyticsMapper;

    public void sendEvent(Long userId, String eventType, String payload) {
        try {
            AnalyticsEventDto event = analyticsMapper.toAnalyticsEventDto(userId, eventType, payload);
            kafkaTemplate.send("analytics-events", userId.toString(), event);
            log.info("Аналитика [{}]: отправлено для пользователя {}", eventType, userId);
        } catch (Exception e) {
            log.error("Ошибка аналитики: {}", e.getMessage());
        }
    }
}