package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.dto.BudgetAnalyticsPayload;
import com.teamfourpixels.entity.*;
import com.teamfourpixels.enums.LimitType;
import com.teamfourpixels.enums.OperationType;
import com.teamfourpixels.mapper.BudgetMapper;
import com.teamfourpixels.repository.BudgetRepository;
import com.teamfourpixels.repository.CategoryRepository;
import com.teamfourpixels.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private final AnalyticsService analyticsService;
    private final AuditService auditService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public BudgetDto createOrUpdateBudget(Long userId, Integer year, Integer month, CreateBudgetRequest request) {
        validateCategoryExistence(request);
        validatePercentLimits(request);
        validateAmountLimits(request);
        validateTotalAllocation(request);

        Budget budget = budgetRepository
                .findByUserIdAndYearAndMonth(userId, year, month)
                .orElse(Budget.builder().build());

        budgetMapper.updateBudgetFields(budget, request, userId, year, month);
        budget.getLimits().clear();

        List<BudgetLimit> newLimits = budgetMapper.toLimitEntities(request.getLimits(), budget);
        budget.getLimits().addAll(newLimits);

        Budget savedBudget = budgetRepository.save(budget);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                analyticsService.sendEvent(userId, "BUDGET_CREATED", "{\"totalIncome\":" + savedBudget.getTotalIncome() + "}");
                auditService.sendAuditLog(userId, "BUDGET_CONFIGURED");
            }
        });

        return budgetMapper.toDto(savedBudget);
    }

    @Override
    @Transactional
    public void processTransactionEvent(TransactionCreatedEvent event) {
        int year = event.getTimestamp().getYear();
        int month = event.getTimestamp().getMonthValue();

        Budget budget = budgetRepository.findByUserIdAndYearAndMonth(event.getUserId(), year, month)
                .orElse(null);

        if (budget == null) return;

        budget.getLimits().stream()
                .filter(limit -> limit.getCategoryId().equals(event.getCategoryId()))
                .findFirst()
                .ifPresent(limit -> checkLimitAndNotify(budget, limit));
    }

    private void checkLimitAndNotify(Budget budget, BudgetLimit limit) {
        BigDecimal totalSpent = transactionRepository.getTotalSpentByCategory(
                budget.getUserId(), limit.getCategoryId(), OperationType.EXPENSE
        );

        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        BigDecimal limitValue = limit.getLimitValue();
        if (limit.getLimitType() == LimitType.PERCENT) {
            limitValue = budget.getTotalIncome()
                    .multiply(limitValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        int currentPercent = totalSpent.divide(limitValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).intValue();

        if (currentPercent >= 100 && !limit.is100Notified()) {
            limit.set100Notified(true);
            limit.set80Notified(true);
            budgetRepository.save(budget);
            sendAlert(budget.getUserId(), limit, totalSpent, limitValue, currentPercent);

        } else if (currentPercent >= 80 && currentPercent < 100 && !limit.is80Notified()) {
            limit.set80Notified(true);
            budgetRepository.save(budget);
            sendAlert(budget.getUserId(), limit, totalSpent, limitValue, currentPercent);
        }
    }

    private void sendAlert(Long userId, BudgetLimit limit, BigDecimal spent, BigDecimal limitVal, int percent) {
        String categoryName = categoryRepository.findById(limit.getCategoryId())
                .map(Category::getName)
                .orElse("Категория " + limit.getCategoryId());

        BudgetLimitEvent alert = BudgetLimitEvent.builder()
                .userId(userId)
                .categoryId(limit.getCategoryId())
                .categoryName(categoryName)
                .limitAmount(limitVal)
                .currentSpent(spent)
                .percentage(percent)
                .build();

        kafkaTemplate.send("budget-limit-events", userId.toString(), alert);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDto getBudget(Long userId, Integer year, Integer month) {
        return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(budgetMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found."));
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Integer year, Integer month) {
        budgetRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found"));
        budgetRepository.deleteByUserIdAndYearAndMonth(userId, year, month);

        auditService.sendAuditLog(userId, "BUDGET_DELETED");
    }

    private void validatePercentLimits(CreateBudgetRequest request) {
        BigDecimal percentSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.PERCENT)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (percentSum.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Сумма процентных лимитов > 100%.");
        }
    }

    private void validateAmountLimits(CreateBudgetRequest request) {
        BigDecimal amountSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.SUM)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amountSum.compareTo(request.getTotalIncome()) > 0) {
            throw new IllegalArgumentException("Сумма фиксированных лимитов > дохода.");
        }
    }

    private void validateTotalAllocation(CreateBudgetRequest request) {
        BigDecimal totalIncome = request.getTotalIncome();
        BigDecimal percentSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.PERCENT)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amountSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.SUM)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal percentAsMoney = totalIncome.multiply(percentSum).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAllocated = percentAsMoney.add(amountSum);
        
        if (totalAllocated.compareTo(totalIncome) > 0) {
            throw new IllegalArgumentException("Общая сумма лимитов превышает доход.");
        }
    }

    private void validateCategoryExistence(CreateBudgetRequest request) {
        Set<Long> ids = request.getLimits().stream().map(LimitDto::getCategoryId).collect(Collectors.toSet());
        if (ids.isEmpty()) return;
        if (categoryRepository.findByIdIn(ids.stream().toList()).size() != ids.size()) {
            throw new IllegalArgumentException("Одна или несколько категорий не найдены.");
        }
    }
}
