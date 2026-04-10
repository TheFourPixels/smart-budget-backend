package com.teamfourpixels.service;

import com.teamfourpixels.dto.AuditEventDto;
import com.teamfourpixels.dto.BudgetLimitEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;

    @KafkaListener(topics = "audit-events", groupId = "audit-group")
    public void consumeAuditEvent(AuditEventDto eventDto) {
        log.info("Получено событие аудита: {} для транзакции {}",
                eventDto.getAction(), eventDto.getTransactionId());
        auditService.saveAuditEvent(eventDto);
    }

    @KafkaListener(topics = "budget-limit-events", groupId = "audit-group")
    public void consumeBudgetLimitEvent(BudgetLimitEvent eventDto) {
        log.info("Аудит поймал событие достижения лимита: user={}, израсходовано={}%",
                eventDto.getUserId(), eventDto.getPercentage());
        auditService.saveLimitEvent(eventDto);
    }
}