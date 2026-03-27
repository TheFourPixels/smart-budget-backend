package com.teamfourpixels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.teamfourpixels.dto.AuditEventDto;
import com.teamfourpixels.dto.BudgetLimitEvent;
import com.teamfourpixels.entity.AuditLog;
import com.teamfourpixels.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "audit-events", groupId = "audit-group")
    public void consumeAuditEvent(String payload) {
        try {
            objectMapper.registerModule(new JavaTimeModule());

            AuditEventDto eventDto = objectMapper.readValue(payload, AuditEventDto.class);
            log.info("Получено событие аудита: {} для транзакции {}", eventDto.getAction(), eventDto.getTransactionId());

            AuditLog auditLog = AuditLog.builder()
                    .eventId(eventDto.getEventId())
                    .action(eventDto.getAction())
                    .userId(eventDto.getUserId())
                    .transactionId(eventDto.getTransactionId())
                    .oldCategoryId(eventDto.getOldCategoryId())
                    .newCategoryId(eventDto.getNewCategoryId())
                    .timestamp(eventDto.getTimestamp())
                    .processedAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Событие успешно сохранено в БД аудита.");

        } catch (Exception e) {
            log.error("Ошибка при обработке события аудита: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "budget-limit-events", groupId = "audit-group")
    public void consumeBudgetLimitEvent(String payload) {
        try {
            objectMapper.registerModule(new JavaTimeModule());

            BudgetLimitEvent eventDto = objectMapper.readValue(payload, BudgetLimitEvent.class);
            log.info("Аудит поймал событие достижения лимита: user={}, category={}, израсходовано={}%",
                    eventDto.getUserId(), eventDto.getCategoryId(), eventDto.getPercentage());

            AuditLog auditLog = AuditLog.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .action("LIMIT_REACHED_" + eventDto.getPercentage())
                    .userId(eventDto.getUserId())
                    .newCategoryId(eventDto.getCategoryId())
                    .timestamp(LocalDateTime.now())
                    .processedAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Событие лимита успешно сохранено в БД аудита.");

        } catch (Exception e) {
            log.error("Ошибка при обработке события лимита в аудите: {}", e.getMessage());
        }
    }
}