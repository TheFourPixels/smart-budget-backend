package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.AuditEventDto;
import com.teamfourpixels.dto.BudgetLimitEvent;
import com.teamfourpixels.entity.AuditLog;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AuditMapper {

    public AuditLog toEntity(AuditEventDto dto) {
        return AuditLog.builder()
                .eventId(dto.getEventId())
                .action(dto.getAction())
                .userId(dto.getUserId())
                .transactionId(dto.getTransactionId())
                .oldCategoryId(dto.getOldCategoryId())
                .newCategoryId(dto.getNewCategoryId())
                .timestamp(dto.getTimestamp())
                .processedAt(LocalDateTime.now())
                .build();
    }

    public AuditLog toEntity(BudgetLimitEvent event) {
        return AuditLog.builder()
                .eventId(UUID.randomUUID().toString())
                .action("LIMIT_REACHED_" + event.getPercentage())
                .userId(event.getUserId())
                .newCategoryId(event.getCategoryId())
                .timestamp(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();
    }
}