package com.teamfourpixels.service;

import com.teamfourpixels.dto.CreateTransactionRequest;
import com.teamfourpixels.dto.TransactionDto;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.enums.OperationType;
import com.teamfourpixels.mapper.TransactionMapper;
import com.teamfourpixels.repository.TransactionRepository;
import com.teamfourpixels.service.classification.ClassificationStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async; // ✅ ИМПОРТ
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture; // ✅ ИМПОРТ

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final BankServiceClient bankServiceClient;
    private final CategoryQueryService categoryQueryService;
    private final List<ClassificationStrategy> classificationStrategies;

    private static final Long UNCATEGORIZED_ID = 999L;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsPage(Long userId, int page, int size,
                                                    Long categoryId, String query,
                                                    Instant start, Instant end) {
        Specification<Transaction> spec = new TransactionFilterSpecification(
                userId, categoryId, query, start, end);

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionTime").descending());
        Page<Transaction> pageResult = repository.findAll(spec, pageable);
        return pageResult.map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionDetails(Long userId, Long id) {
        return mapper.toDto(getByIdAndUser(id, userId));
    }

    @Async
    @Transactional
    public CompletableFuture<Integer> importAndClassifyTransactions(Long userId, int year, int month) {
        List<TransactionDto> bankTransactions = bankServiceClient.fetchTransactions(year, month);
        int newCount = 0;

        for (TransactionDto bankDto : bankTransactions) {
            if (repository.findByUserIdAndBankTransactionRefId(userId, bankDto.getExternalId()).isPresent()) {
                continue;
            }
            Transaction transaction = convertBankDtoToTransaction(userId, bankDto);

            Long categoryId = autoClassify(transaction);
            transaction.setCategoryId(categoryId);
            repository.save(transaction);
            newCount++;
        }

        return CompletableFuture.completedFuture(newCount);
    }

    private Transaction convertBankDtoToTransaction(Long userId, TransactionDto bankDto) {
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
                .bankTransactionRefId(bankDto.getExternalId())
                .build();
    }

    private Long autoClassify(Transaction t) {
        for (ClassificationStrategy strategy : classificationStrategies) {
            Optional<Long> categoryId = strategy.classify(t);
            if (categoryId.isPresent()) {
                return categoryId.get();
            }
        }
        return UNCATEGORIZED_ID;
    }

    @Transactional
    public TransactionDto updateTransactionCategory(Long userId, Long id, Long categoryId) {
        Transaction t = getByIdAndUser(id, userId);

        try {
            categoryQueryService.getCategoryById(categoryId);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(
                    "Ошибка: Невозможно изменить категорию. Категория с ID " + categoryId + " не найдена."
            );
        }

        t.setCategoryId(categoryId);
        return mapper.toDto(repository.save(t));
    }

    private Transaction getByIdAndUser(Long id, Long userId) {
        return repository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Транзакция не найдена"));
    }

    @Transactional
    public TransactionDto createTransaction(Long userId, CreateTransactionRequest request) {
        Transaction t = mapper.toEntity(request, userId);
        return mapper.toDto(repository.save(t));
    }

    @Transactional
    public TransactionDto updateTransaction(Long userId, Long id, CreateTransactionRequest request) {
        Transaction t = getByIdAndUser(id, userId);
        mapper.updateEntity(request, t);
        return mapper.toDto(repository.save(t));
    }
}