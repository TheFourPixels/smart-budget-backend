package com.teamfourpixels.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEventDto {
    private String eventId;
    private String eventType;
    private Long userId;
    private LocalDateTime timestamp;
    private String platform;
    private String payload;
}