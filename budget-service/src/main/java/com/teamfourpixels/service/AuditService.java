package com.teamfourpixels.service;

import com.teamfourpixels.mapper.AuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuditMapper auditMapper;
    private static final String AUDIT_TOPIC = "audit-events";

    public void sendAuditLog(Long userId, String action) {
        var event = auditMapper.toAuditEventDto(userId, action);
        kafkaTemplate.send(AUDIT_TOPIC, userId.toString(), event);
    }
}