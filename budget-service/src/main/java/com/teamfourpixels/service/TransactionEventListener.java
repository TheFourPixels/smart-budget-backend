package com.teamfourpixels.service;

import com.teamfourpixels.dto.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final BudgetServiceImpl budgetService;

    @KafkaListener(topics = "transaction-events", groupId = "budget-group")
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Received transaction event for analysis: user={}, category={}",
                event.getUserId(), event.getCategoryId());

        try {
            budgetService.processTransactionEvent(event);
        } catch (Exception e) {
            log.error("Error analyzing budget limits: {}", e.getMessage());
        }
    }
}