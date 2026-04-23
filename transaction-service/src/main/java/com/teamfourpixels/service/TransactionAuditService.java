package com.teamfourpixels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfourpixels.entity.OutboxEvent;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.mapper.TransactionMapper;
import com.teamfourpixels.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionAuditService {
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY) // Должно выполняться в транзакции сервиса
    public void saveAuditToOutbox(Transaction transaction, String action, Long oldCategoryId) {
        try {
            var eventDto = mapper.toAuditEventDto(transaction, action, oldCategoryId);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateId(transaction.getId().toString())
                    .eventType(action)
                    .payload(objectMapper.writeValueAsString(eventDto))
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Ошибка сохранения в Outbox: {}", e.getMessage());
        }
    }
}