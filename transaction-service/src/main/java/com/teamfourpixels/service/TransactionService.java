package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.entity.TransactionSplit;
import com.teamfourpixels.enums.OperationType;
import com.teamfourpixels.mapper.TransactionMapper;
import com.teamfourpixels.repository.TransactionRepository;
import com.teamfourpixels.repository.TransactionSplitRepository;
import com.teamfourpixels.service.classification.ClassificationStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    private final AnalyticsService analyticsService;
    private final TransactionAuditService auditService;
    private final TransactionMetricsService metricsService;
    private final TransactionEventPublisher eventPublisher;

    private static final Long UNCATEGORIZED_ID = 999L;
    private static final Long SPLIT_CATEGORY_ID = -1L;

    @Transactional
    public TransactionDto createTransaction(Long userId, CreateTransactionRequest request) {
        Transaction t = mapper.toEntity(request, userId);
        Transaction saved = repository.save(t);

        if (saved.getType() == OperationType.EXPENSE) {
            eventPublisher.publishCreatedEvent(saved);
        }

        auditService.saveAuditToOutbox(saved, "TRANSACTION_CREATED", null);
        metricsService.incrementManualTransactions();

        analyticsService.sendEvent(userId, "TRANSACTION_ADDED",
                String.format("{\"amount\":%s, \"type\":\"%s\", \"source\":\"manual\", \"categoryId\":%d}",
                        saved.getAmount(), saved.getType(), saved.getCategoryId()));

        return mapper.toDto(saved);
    }

    @Async
    @Transactional
    public CompletableFuture<Integer> importAndClassifyTransactions(Long userId, int year, int month) {
        return CompletableFuture.completedFuture(metricsService.recordBankSync(() -> {
            log.info("Начинаем импорт транзакций для пользователя {} за {}/{}", userId, month, year);
            List<TransactionDto> bankTransactions = bankServiceClient.fetchTransactions(year, month);

            if (bankTransactions == null || bankTransactions.isEmpty()) return 0;

            int newCount = 0;
            List<ClassificationStrategy> sortedStrategies = classificationStrategies.stream()
                    .sorted(Comparator.comparingInt(ClassificationStrategy::getPriority))
                    .toList();

            for (TransactionDto bankDto : bankTransactions) {
                String uniqueRefId = bankDto.getExternalId() + "-u" + userId;
                if (repository.findByUserIdAndBankTransactionRefId(userId, uniqueRefId).isPresent()) continue;

                metricsService.incrementAutoImportedTransactions();
                Transaction transaction = convertBankDtoToTransaction(userId, bankDto, uniqueRefId);

                Long categoryId = autoClassify(transaction, sortedStrategies);
                transaction.setCategoryId(categoryId);

                if (UNCATEGORIZED_ID.equals(categoryId)) {
                    metricsService.incrementClassificationFailed();
                } else {
                    metricsService.incrementClassificationSuccess();
                }

                Transaction saved = repository.save(transaction);

                analyticsService.sendEvent(userId, "TRANSACTION_ADDED",
                        String.format("{\"amount\":%s, \"type\":\"%s\", \"source\":\"auto\", \"categoryId\":%d}",
                                saved.getAmount(), saved.getType(), saved.getCategoryId()));

                if (saved.getType() == OperationType.EXPENSE) {
                    eventPublisher.publishCreatedEvent(saved);
                }

                auditService.saveAuditToOutbox(saved, "TRANSACTION_IMPORTED", null);
                newCount++;
            }
            return newCount;
        }));
    }

    @Transactional
    public TransactionDto splitTransaction(Long userId, Long transactionId, SplitTransactionRequest request) {
        Transaction transaction = getByIdAndUser(transactionId, userId);
        Long oldCategoryId = transaction.getCategoryId();

        validateSplitSum(transaction, request);

        splitRepository.deleteAll(splitRepository.findByTransactionId(transactionId));

        List<TransactionSplit> newSplits = request.getSplits().stream()
                .map(req -> mapper.toSplitEntity(req, transaction))
                .toList();
        splitRepository.saveAll(newSplits);

        transaction.setCategoryId(SPLIT_CATEGORY_ID);
        repository.save(transaction);

        auditService.saveAuditToOutbox(transaction, "TRANSACTION_SPLIT", oldCategoryId);
        return mapper.toDto(transaction);
    }

    @Transactional
    public TransactionDto updateTransactionCategory(Long userId, Long id, Long categoryId) {
        Transaction t = getByIdAndUser(id, userId);
        Long oldCategoryId = t.getCategoryId();

        if (t.getCategoryId().equals(SPLIT_CATEGORY_ID)) {
            splitRepository.deleteAll(splitRepository.findByTransactionId(id));
        }

        validateCategory(categoryId);

        if (!categoryId.equals(oldCategoryId)) {
            if (t.getBankTransactionRefId() != null) {
                analyticsService.sendEvent(userId, "AUTO_CATEGORY_CORRECTED",
                        String.format("{\"transactionId\":%d, \"oldCat\":%d, \"newCat\":%d}",
                                t.getId(), oldCategoryId, categoryId));
            }
            t.setCategoryId(categoryId);
            repository.save(t);
            auditService.saveAuditToOutbox(t, "CATEGORY_CHANGED", oldCategoryId);
        }
        return mapper.toDto(t);
    }

    private void validateSplitSum(Transaction t, SplitTransactionRequest req) {
        BigDecimal total = req.getSplits().stream()
                .map(SplitTransactionRequest.SplitPartRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(t.getAmount().abs()) != 0) {
            throw new IllegalArgumentException("Сумма частей не совпадает с суммой транзакции");
        }
    }

    private void validateCategory(Long categoryId) {
        try {
            categoryQueryService.getCategoryById(categoryId);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException("Категория не найдена.");
        }
    }

    private Long autoClassify(Transaction t, List<ClassificationStrategy> strategies) {
        return strategies.stream()
                .map(s -> s.classify(t))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(UNCATEGORIZED_ID);
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

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsPage(Long userId, int page, int size, Long categoryId, String query, Instant start, Instant end) {
        Specification<Transaction> spec = new TransactionFilterSpecification(userId, categoryId, query, start, end);
        return repository.findAll(spec, PageRequest.of(page, size, Sort.by("transactionTime").descending())).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionDetails(Long userId, Long id) {
        return mapper.toDto(getByIdAndUser(id, userId));
    }

    private Transaction getByIdAndUser(Long id, Long userId) {
        return repository.findById(id).filter(t -> t.getUserId().equals(userId)).orElseThrow(() -> new EntityNotFoundException("Транзакция не найдена"));
    }

    @Transactional(readOnly = true)
    public CategoryTotalSpentDto getTotalSpentByCategory(Long userId, Long categoryId) {
        BigDecimal total = repository.getTotalSpentByCategory(userId, categoryId, OperationType.EXPENSE);
        return new CategoryTotalSpentDto(categoryId, total == null ? BigDecimal.ZERO : total);
    }
}