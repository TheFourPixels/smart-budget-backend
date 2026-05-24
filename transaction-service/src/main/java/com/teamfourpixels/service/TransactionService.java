package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.dto.TransactionAnalyticsPayload;
import com.teamfourpixels.dto.AutoCategoryCorrectionPayload;
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
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private final ConcurrentHashMap<Long, ReentrantLock> importLocks = new ConcurrentHashMap<>();

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
                TransactionAnalyticsPayload.builder()
                        .amount(saved.getAmount())
                        .type(saved.getType())
                        .source("manual")
                        .categoryId(saved.getCategoryId())
                        .build());

        TransactionDto dto = mapper.toDto(saved);
        enrichWithCategories(Collections.singletonList(dto));
        return dto;
    }

    @Async
    public CompletableFuture<Integer> importAndClassifyTransactions(Long userId, int year, int month) {
        return CompletableFuture.supplyAsync(() -> {
            ReentrantLock lock = importLocks.computeIfAbsent(userId, k -> new ReentrantLock());
            if (!lock.tryLock()) {
                log.warn("Импорт для пользователя {} уже запущен", userId);
                return 0;
            }
            try {
                return metricsService.recordBankSync(() -> {
                    log.info("Начинаем импорт транзакций для пользователя {} за {}/{}", userId, month, year);
            List<TransactionDto> bankTransactions = bankServiceClient.fetchTransactions(year, month);

            if (bankTransactions == null || bankTransactions.isEmpty()) return 0;

            int newCount = 0;
            List<ClassificationStrategy> sortedStrategies = classificationStrategies.stream()
                    .sorted(Comparator.comparingInt((ClassificationStrategy s) -> s.getPriority(userId)))
                    .toList();

            for (TransactionDto bankDto : bankTransactions) {
                String uniqueRefId = bankDto.getExternalId() + "-u" + userId;
                if (repository.findByUserIdAndBankTransactionRefId(userId, uniqueRefId).isPresent()) continue;

                metricsService.incrementAutoImported();
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
                        TransactionAnalyticsPayload.builder()
                                .amount(saved.getAmount())
                                .type(saved.getType())
                                .source("auto")
                                .categoryId(saved.getCategoryId())
                                .build());

                if (saved.getType() == OperationType.EXPENSE) {
                    eventPublisher.publishCreatedEvent(saved);
                }

                auditService.saveAuditToOutbox(saved, "TRANSACTION_IMPORTED", null);
                newCount++;
            }
            return newCount;
        });
            } finally {
                lock.unlock();
            }
        });
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
        TransactionDto dto = mapper.toDto(transaction);
        enrichWithCategories(Collections.singletonList(dto));
        return dto;
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
                metricsService.incrementAutoCorrected();
                analyticsService.sendEvent(userId, "AUTO_CATEGORY_CORRECTED",
                        AutoCategoryCorrectionPayload.builder()
                                .transactionId(t.getId())
                                .oldCat(oldCategoryId)
                                .newCat(categoryId)
                                .build());
            }
            t.setCategoryId(categoryId);
            repository.save(t);
            auditService.saveAuditToOutbox(t, "CATEGORY_CHANGED", oldCategoryId);
        }
        TransactionDto dto = mapper.toDto(t);
        enrichWithCategories(Collections.singletonList(dto));
        return dto;
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
        Page<Transaction> entities = repository.findAll(spec, PageRequest.of(page, size, Sort.by("transactionTime").descending()));

        List<TransactionDto> dtos = entities.stream().map(mapper::toDto).toList();
        enrichWithCategories(dtos);

        return new PageImpl<>(dtos, entities.getPageable(), entities.getTotalElements());
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionDetails(Long userId, Long id) {
        TransactionDto dto = mapper.toDto(getByIdAndUser(id, userId));
        enrichWithCategories(Collections.singletonList(dto));
        return dto;
    }

    private void enrichWithCategories(List<TransactionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        List<Long> catIds = dtos.stream()
                .map(dto -> dto.getCategory() != null ? dto.getCategory().getId() : null)
                .filter(id -> id != null && id > 0 && !id.equals(UNCATEGORIZED_ID) && !id.equals(SPLIT_CATEGORY_ID))
                .distinct()
                .toList();

        if (catIds.isEmpty()) return;

        try {
            Map<Long, CategoryDto> catMap = categoryQueryService.getCategoriesByIds(catIds).stream()
                    .collect(Collectors.toMap(CategoryDto::getId, c -> c, (v1, v2) -> v1));

            dtos.forEach(dto -> {
                if (dto.getCategory() != null && catMap.containsKey(dto.getCategory().getId())) {
                    dto.setCategory(catMap.get(dto.getCategory().getId()));
                } else if (dto.getCategory() != null && !dto.getCategory().getId().equals(UNCATEGORIZED_ID) && !dto.getCategory().getId().equals(SPLIT_CATEGORY_ID)) {
                    dto.getCategory().setName("Категория не найдена");
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при пакетной загрузке категорий: {}", e.getMessage());
        }
    }

    private Transaction getByIdAndUser(Long id, Long userId) {
        return repository.findById(id).filter(t -> t.getUserId().equals(userId)).orElseThrow(() -> new EntityNotFoundException("Транзакция не найдена"));
    }

    @Transactional(readOnly = true)
    public CategoryTotalSpentDto getTotalSpentByCategory(Long userId, Long categoryId) {
        BigDecimal total = repository.getTotalSpentByCategory(userId, categoryId, OperationType.EXPENSE);
        return new CategoryTotalSpentDto(categoryId, total == null ? BigDecimal.ZERO : total);
    }

    @Transactional(readOnly = true)
    public void exportTransactionsToCsv(Long userId, Long categoryId, String query, Instant start, Instant end, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"transactions.csv\"");
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"ID", "Date", "Amount", "Type", "Category ID", "Merchant", "Description"});
            Specification<Transaction> spec = new TransactionFilterSpecification(userId, categoryId, query, start, end);
            List<Transaction> transactions = repository.findAll(spec, Sort.by("transactionTime").descending());
            for (Transaction t : transactions) {
                writer.writeNext(new String[]{
                        String.valueOf(t.getId()),
                        t.getTransactionTime() != null ? t.getTransactionTime().toString() : "",
                        t.getAmount() != null ? t.getAmount().toString() : "",
                        t.getType() != null ? t.getType().name() : "",
                        t.getCategoryId() != null ? t.getCategoryId().toString() : "",
                        t.getMerchant() != null ? t.getMerchant() : "",
                        t.getDescription() != null ? t.getDescription() : ""
                });
            }
        } catch (Exception e) {
            log.error("Не удалось экспортировать транзакции в CSV для пользователя {}", userId, e);
            throw new RuntimeException("Ошибка при экспорте CSV", e);
        }
    }
}
