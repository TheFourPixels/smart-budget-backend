package com.teamfourpixels.service;

import com.teamfourpixels.dto.TransactionCreatedEvent;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionMapper mapper;

    private static final String TRANSACTION_TOPIC = "transaction-events";

    public void publishCreatedEvent(Transaction transaction) {
        try {
            TransactionCreatedEvent event = mapper.toKafkaEvent(transaction);
            kafkaTemplate.send(TRANSACTION_TOPIC, transaction.getUserId().toString(), event);
            log.debug("Событие транзакции {} отправлено в Kafka", transaction.getId());
        } catch (Exception e) {
            log.error("Ошибка при публикации события транзакции {}: {}", transaction.getId(), e.getMessage());
        }
    }
}