package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.AnalyticsEventDto;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AnalyticsMapper {
    public AnalyticsEventDto toAnalyticsEventDto(Long userId, String eventType, String payload) {
        return AnalyticsEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .platform("BACKEND")
                .payload(payload)
                .build();
    }
}