package com.teamfourpixels.service;

import com.teamfourpixels.dto.CategoryDeletedEvent;
import com.teamfourpixels.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryEventListener {

    private final TransactionRepository transactionRepository;
    private static final Long UNCATEGORIZED_ID = 999L;

    @Transactional
    @KafkaListener(topics = "category-events", groupId = "transaction-group")
    public void consumeCategoryDeleted(CategoryDeletedEvent event) {
        log.info("Получено событие удаления категории: categoryId={}, userId={}", 
                event.getCategoryId(), event.getUserId());

        transactionRepository.reassignCategoryToUncategorized(
                event.getCategoryId(),
                event.getUserId(),
                UNCATEGORIZED_ID
        );
        log.info("Транзакции категории {} успешно переназначены на Uncategorized (999)", event.getCategoryId());
    }
}
