package com.teamfourpixels.service;

import com.teamfourpixels.entity.OutboxEvent;
import com.teamfourpixels.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OutboxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private static final String AUDIT_TOPIC = "audit-events";

    public OutboxScheduler(OutboxEventRepository outboxEventRepository,
                           @Qualifier("stringKafkaTemplate") KafkaTemplate<String, String> stringKafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.stringKafkaTemplate = stringKafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                stringKafkaTemplate.send(AUDIT_TOPIC, event.getAggregateId(), event.getPayload());
                event.setStatus("PROCESSED");
                outboxEventRepository.save(event);
                log.info("Событие {} успешно отправлено в Kafka", event.getId());
            } catch (Exception e) {
                log.error("Ошибка при отправке события {} в Kafka: {}", event.getId(), e.getMessage());
                break;
            }
        }
    }
}