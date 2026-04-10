package com.teamfourpixels.service;

import com.teamfourpixels.entity.OutboxEvent;
import com.teamfourpixels.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Qualifier("stringKafkaTemplate")
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String AUDIT_TOPIC = "audit-events";

    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findAll();
        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(AUDIT_TOPIC, event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                outboxEventRepository.delete(event);
                                log.info("Событие аудита успешно отправлено.");
                            } else {
                                log.error("Ошибка Kafka: {}", ex.getMessage());
                            }
                        });
            } catch (Exception e) {
                log.error("Критический сбой: {}", e.getMessage());
            }
        }
    }
}