package com.teamfourpixels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfourpixels.entity.OutboxEvent;
import com.teamfourpixels.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Map<String, String> TOPIC_MAP = Map.of(
            "BUDGET_LIMIT_ALERT", "budget-limit-events",
            "BUDGET_AUDIT", "audit-events",
            "ANALYTICS_EVENT", "analytics-events"
    );

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvent(String aggregateId, String type, Object payload) {
        try {
            OutboxEvent event = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateId(aggregateId)
                    .eventType(type)
                    .payload(objectMapper.writeValueAsString(payload))
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error("Ошибка сохранения в Outbox: {}", e.getMessage());
        }
    }

    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findAll();
        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            String topic = TOPIC_MAP.getOrDefault(event.getEventType(), "audit-events");
            try {
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                outboxEventRepository.delete(event);
                            } else {
                                log.error("Ошибка Kafka для {}: {}", topic, ex.getMessage());
                            }
                        });
            } catch (Exception e) {
                log.error("Критический сбой при отправке в Kafka: {}", e.getMessage());
            }
        }
    }
}
