package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.AuditEventDto;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AuditMapper {
    public AuditEventDto toAuditEventDto(Long userId, String action) {
        return AuditEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .action(action)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}