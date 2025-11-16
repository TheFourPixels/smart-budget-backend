package com.teamfourpixels.service;
import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.enums.OperationType;
import com.teamfourpixels.mapper.TransactionMapper;
import com.teamfourpixels.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final BankServiceClient bankServiceClient;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsPage(Long userId, int page, int size,
                                                    Long categoryId, String query,
                                                    Instant start, Instant end) {
        Specification<Transaction> spec = Specification.where(TransactionSpecs.hasUserId(userId))
                .and(TransactionSpecs.betweenDates(start, end));
        if (categoryId != null) spec = spec.and(TransactionSpecs.hasCategoryId(categoryId));
        if (query != null && !query.isBlank()) spec = spec.and(TransactionSpecs.merchantOrDescriptionContains(query));
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionTime").descending());
        Page<Transaction> pageResult = repository.findAll(spec, pageable);
        return pageResult.map(mapper::toDto);
    }
    @Transactional(readOnly = true)
    public TransactionDto getTransactionDetails(Long userId, Long id) {
        return mapper.toDto(getByIdAndUser(id, userId));
    }

    @Transactional
    public int importAndClassifyTransactions(Long userId, int year, int month) {
        List<TransactionDto> bankTransactions = bankServiceClient.fetchTransactions(year, month);
        int newCount = 0;
        for (TransactionDto bankDto : bankTransactions) {
            if (repository.findByUserIdAndBankTransactionRefId(userId, bankDto.getExternal_id()).isPresent()) {
                continue;
            }
            Transaction transaction = convertBankDtoToTransaction(userId, bankDto);
            Long categoryId = autoClassify(transaction);
            transaction.setCategoryId(categoryId);
            repository.save(transaction);
            newCount++;
        }
        return newCount;
    }
    private Transaction convertBankDtoToTransaction(Long userId, TransactionDto bankDto) {
        BigDecimal absAmount = bankDto.getAmount().abs().setScale(2, RoundingMode.HALF_UP);
        OperationType type = bankDto.getAmount().signum() >= 0 ? OperationType.INCOME : OperationType.EXPENSE;
        return Transaction.builder()
                .userId(userId)
                .transactionTime(bankDto.getTransaction_date())
                .amount(absAmount)
                .type(type)
                .mcc(bankDto.getMcc())
                .merchant(bankDto.getMerchant_name())
                .description(bankDto.getDescription())
                .isSplit(false)
                .bankTransactionRefId(bankDto.getExternal_id())
                .build();
    }

    private Long autoClassify(Transaction t) {
        String mcc = t.getMcc();
        String merchant = t.getMerchant();

        if (mcc != null) {
            return switch (mcc) {
                case "5411" -> 1L;   // Продукты
                case "5812" -> 10L;  // Рестораны
                case "4900" -> 8L;   // Коммунальные
                case "5541" -> 4L;   // Проезд
                case "5651" -> 3L;   // Одежда
                default -> 999L;
            };
        }

        if (merchant != null && merchant.contains("Зарплата")) {
            return 1L;
        }

        return 999L;
    }

    @Transactional
    public List<TransactionDto> splitTransaction(Long userId, Long originalId, List<SplitPartDto> parts) {
        Transaction original = getByIdAndUser(originalId, userId);
        if (parts.isEmpty()) throw new IllegalArgumentException("Должна быть хотя бы одна часть");
        BigDecimal sum = parts.stream().map(SplitPartDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(original.getAmount()) != 0) {
            throw new IllegalArgumentException("Сумма частей не совпадает");
        }
        List<Transaction> newParts = new ArrayList<>();
        for (SplitPartDto p : parts) {
            Transaction part = Transaction.builder()
                    .userId(userId)
                    .transactionTime(original.getTransactionTime())
                    .amount(p.getAmount())
                    .type(original.getType())
                    .mcc(original.getMcc())
                    .merchant(original.getMerchant())
                    .categoryId(p.getCategoryId() != null ? p.getCategoryId() : 999L)
                    .description(p.getDescription() != null ? p.getDescription() : original.getDescription())
                    .isSplit(true)
                    .originalTransactionId(originalId)
                    .bankTransactionRefId(original.getBankTransactionRefId())
                    .build();
            newParts.add(part);
        }
        repository.delete(original);
        return mapper.toDtoList(repository.saveAll(newParts));
    }
    @Transactional
    public TransactionDto updateTransactionCategory(Long userId, Long id, Long categoryId) {
        Transaction t = getByIdAndUser(id, userId);
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