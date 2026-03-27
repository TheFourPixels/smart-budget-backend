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
public class AuditEventDto {
    private String eventId;
    private String action;
    private Long userId;
    private Long transactionId;
    private Long oldCategoryId;
    private Long newCategoryId;
    private LocalDateTime timestamp;
}