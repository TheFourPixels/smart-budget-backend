package com.teamfourpixels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.OutboxEvent;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.entity.TransactionSplit;
import com.teamfourpixels.enums.OperationType;
import com.teamfourpixels.mapper.TransactionMapper;
import com.teamfourpixels.repository.OutboxEventRepository;
import com.teamfourpixels.repository.TransactionRepository;
import com.teamfourpixels.repository.TransactionSplitRepository;
import com.teamfourpixels.service.classification.ClassificationStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionSplitRepository splitRepository;
    private final TransactionMapper mapper;
    private final BankServiceClient bankServiceClient;
    private final CategoryQueryService categoryQueryService;
    private final List<ClassificationStrategy> classificationStrategies;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final TransactionMetricsService metricsService;

    private static final String ANALYTICS_TOPIC = "analytics-events";
    private static final Long UNCATEGORIZED_ID = 999L;
    private static final Long SPLIT_CATEGORY_ID = -1L;


    private void saveAuditToOutbox(Transaction transaction, String action, Long oldCategoryId) {
        try {
            AuditEventDto eventDto = mapper.toAuditEventDto(transaction, action, oldCategoryId);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .aggregateId(transaction.getId().toString())
                    .eventType(action)
                    .payload(objectMapper.writeValueAsString(eventDto))
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Событие {} успешно сохранено в Outbox для транзакции {}", action, transaction.getId());
        } catch (Exception e) {
            log.error("Ошибка при подготовке события Outbox ({}): {}", action, e.getMessage());
        }
    }

    @Transactional
    public TransactionDto createTransaction(Long userId, CreateTransactionRequest request) {
        Transaction t = mapper.toEntity(request, userId);
        Transaction saved = repository.save(t);

        if (saved.getType() == OperationType.EXPENSE) {
            sendTransactionEvent(saved);
        }

        saveAuditToOutbox(saved, "TRANSACTION_CREATED", null);
        metricsService.manualTransactionCounter.increment();

        sendAnalyticsEvent(userId, "TRANSACTION_ADDED",
                String.format("{\"amount\":%s, \"type\":\"%s\", \"source\":\"manual\", \"categoryId\":%d}",
                        saved.getAmount(), saved.getType(), saved.getCategoryId()));

        return mapper.toDto(saved);
    }

    @Async
    @Transactional
    public CompletableFuture<Integer> importAndClassifyTransactions(Long userId, int year, int month) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Начинаем импорт транзакций для пользователя {} за {}/{}", userId, month, year);
            List<TransactionDto> bankTransactions = bankServiceClient.fetchTransactions(year, month);

            if (bankTransactions == null || bankTransactions.isEmpty()) {
                metricsService.bankSyncTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
                return CompletableFuture.completedFuture(0);
            }

            int newCount = 0;
            List<ClassificationStrategy> sortedStrategies = classificationStrategies.stream()
                    .sorted(Comparator.comparingInt(ClassificationStrategy::getPriority))
                    .toList();

            for (TransactionDto bankDto : bankTransactions) {
                String uniqueRefId = bankDto.getExternalId() + "-u" + userId;

                if (repository.findByUserIdAndBankTransactionRefId(userId, uniqueRefId).isPresent()) {
                    continue;
                }

                metricsService.autoImportedTransactionCounter.increment();

                Transaction transaction = convertBankDtoToTransaction(userId, bankDto, uniqueRefId);
                Long categoryId = autoClassify(transaction, sortedStrategies);
                transaction.setCategoryId(categoryId);

                if (UNCATEGORIZED_ID.equals(categoryId)) {
                    metricsService.autoClassificationFailedCounter.increment();
                } else {
                    metricsService.autoClassificationSuccessCounter.increment();
                }

                Transaction saved = repository.save(transaction);

                sendAnalyticsEvent(userId, "TRANSACTION_ADDED",
                        String.format("{\"amount\":%s, \"type\":\"%s\", \"source\":\"auto\", \"categoryId\":%d}",
                                saved.getAmount(), saved.getType(), saved.getCategoryId()));

                if (saved.getType() == OperationType.EXPENSE) {
                    sendTransactionEvent(saved);
                }

                saveAuditToOutbox(saved, "TRANSACTION_IMPORTED", null);
                newCount++;
            }

            metricsService.bankSyncTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
            return CompletableFuture.completedFuture(newCount);

        } catch (Exception e) {
            metricsService.bankSyncTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
            log.error("КРИТИЧЕСКАЯ ОШИБКА импорта: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    private void sendTransactionEvent(Transaction transaction) {
        try {
            TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                    .transactionId(transaction.getId())
                    .userId(transaction.getUserId())
                    .categoryId(transaction.getCategoryId())
                    .amount(transaction.getAmount())
                    .description(transaction.getDescription())
                    .timestamp(LocalDateTime.ofInstant(transaction.getTransactionTime(), ZoneId.systemDefault()))
                    .build();

            kafkaTemplate.send("transaction-events", transaction.getUserId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to send Kafka event for transaction {}: {}", transaction.getId(), e.getMessage());
        }
    }

    private void sendAnalyticsEvent(Long userId, String eventType, String payload) {
        try {
            AnalyticsEventDto event = AnalyticsEventDto.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .platform("UNKNOWN")
                    .payload(payload)
                    .build();

            kafkaTemplate.send(ANALYTICS_TOPIC, userId.toString(), event);
            log.info("Analytics event {} sent for user {}", eventType, userId);
        } catch (Exception e) {
            log.error("Ошибка отправки аналитики транзакций: {}", e.getMessage());
        }
    }

    @Transactional
    public TransactionDto splitTransaction(Long userId, Long transactionId, SplitTransactionRequest request) {
        Transaction transaction = getByIdAndUser(transactionId, userId);
        Long oldCategoryId = transaction.getCategoryId();

        BigDecimal totalSplitAmount = request.getSplits().stream()
                .map(SplitTransactionRequest.SplitPartRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplitAmount.compareTo(transaction.getAmount().abs()) != 0) {
            throw new IllegalArgumentException("Сумма частей не совпадает с суммой транзакции");
        }

        List<TransactionSplit> existingSplits = splitRepository.findByTransactionId(transactionId);
        splitRepository.deleteAll(existingSplits);

        List<TransactionSplit> newSplits = request.getSplits().stream()
                .map(req -> TransactionSplit.builder()
                        .transaction(transaction)
                        .categoryId(req.getCategoryId())
                        .amount(req.getAmount())
                        .description(req.getDescription())
                        .build())
                .toList();
        splitRepository.saveAll(newSplits);

        transaction.setCategoryId(SPLIT_CATEGORY_ID);
        repository.save(transaction);

        saveAuditToOutbox(transaction, "TRANSACTION_SPLIT", oldCategoryId);

        return mapper.toDto(transaction);
    }

    @Transactional
    public TransactionDto updateTransactionCategory(Long userId, Long id, Long categoryId) {
        Transaction t = getByIdAndUser(id, userId);
        Long oldCategoryId = t.getCategoryId();

        if (t.getCategoryId().equals(SPLIT_CATEGORY_ID)) {
            List<TransactionSplit> existingSplits = splitRepository.findByTransactionId(id);
            splitRepository.deleteAll(existingSplits);
        }

        try {
            categoryQueryService.getCategoryById(categoryId);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException("Категория не найдена.");
        }

        if (!categoryId.equals(oldCategoryId)) {
            if (t.getBankTransactionRefId() != null) {
                log.info("Пользователь {} исправляет автоклассификацию для транзакции {}", userId, id);
                sendAnalyticsEvent(userId, "AUTO_CATEGORY_CORRECTED",
                        String.format("{\"transactionId\":%d, \"oldCat\":%d, \"newCat\":%d}",
                                t.getId(), oldCategoryId, categoryId));
            }

            t.setCategoryId(categoryId);
            t = repository.save(t);

            saveAuditToOutbox(t, "CATEGORY_CHANGED", oldCategoryId);
        }

        return mapper.toDto(t);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsPage(Long userId, int page, int size,
                                                    Long categoryId, String query,
                                                    Instant start, Instant end) {
        Specification<Transaction> spec = new TransactionFilterSpecification(
                userId, categoryId, query, start, end);

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionTime").descending());
        return repository.findAll(spec, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionDetails(Long userId, Long id) {
        return mapper.toDto(getByIdAndUser(id, userId));
    }

    private Transaction convertBankDtoToTransaction(Long userId, TransactionDto bankDto, String uniqueRefId) {
        BigDecimal absAmount = bankDto.getAmount().abs().setScale(2, RoundingMode.HALF_UP);
        OperationType type = bankDto.getAmount().signum() >= 0 ? OperationType.INCOME : OperationType.EXPENSE;
        return Transaction.builder()
                .userId(userId)
                .transactionTime(bankDto.getTransactionDate())
                .amount(absAmount)
                .type(type)
                .mcc(bankDto.getMcc())
                .merchant(bankDto.getMerchantName())
                .description(bankDto.getDescription())
                .bankTransactionRefId(uniqueRefId)
                .build();
    }

    private Long autoClassify(Transaction t, List<ClassificationStrategy> strategies) {
        for (ClassificationStrategy strategy : strategies) {
            Optional<Long> categoryId = strategy.classify(t);
            if (categoryId.isPresent()) {
                return categoryId.get();
            }
        }
        return UNCATEGORIZED_ID;
    }

    private Transaction getByIdAndUser(Long id, Long userId) {
        return repository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Транзакция не найдена"));
    }

    @Transactional(readOnly = true)
    public CategoryTotalSpentDto getTotalSpentByCategory(Long userId, Long categoryId) {
        BigDecimal total = repository.getTotalSpentByCategory(userId, categoryId, OperationType.EXPENSE);
        return new CategoryTotalSpentDto(categoryId, total == null ? BigDecimal.ZERO : total);
    }
}