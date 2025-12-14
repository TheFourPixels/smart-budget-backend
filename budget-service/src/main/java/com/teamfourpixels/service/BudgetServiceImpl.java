package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import com.teamfourpixels.enums.LimitType;
import com.teamfourpixels.mapper.BudgetMapper;
import com.teamfourpixels.repository.BudgetRepository;
import com.teamfourpixels.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final CategoryRepository categoryRepository;

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

        return budgetMapper.toDto(budgetRepository.save(budget));
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDto getBudget(Long userId, Integer year, Integer month) {
        return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(budgetMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Budget for %d/%02d not found.", year, month)));
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Integer year, Integer month) {
        budgetRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found"));

        budgetRepository.deleteByUserIdAndYearAndMonth(userId, year, month);
    }

    private void validatePercentLimits(CreateBudgetRequest request) {
        BigDecimal percentSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.PERCENT)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (percentSum.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Ошибка валидации: Сумма процентных лимитов не должна превышать 100%.");
        }
    }

    private void validateAmountLimits(CreateBudgetRequest request) {
        BigDecimal amountSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.SUM)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (amountSum.compareTo(request.getTotalIncome()) > 0) {
            throw new IllegalArgumentException("Ошибка валидации: Сумма фиксированных лимитов не должна превышать общий доход.");
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

        BigDecimal percentAsMoney = totalIncome.multiply(percentSum)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalAllocated = percentAsMoney.add(amountSum);

        if (totalAllocated.compareTo(totalIncome) > 0) {
            throw new IllegalArgumentException(
                    String.format("Ошибка бюджета: Общая сумма лимитов (%s) превышает доход (%s).",
                            totalAllocated, totalIncome));
        }
    }

    private void validateCategoryExistence(CreateBudgetRequest request) {
        Set<Long> categoryIdsInRequest = request.getLimits().stream()
                .map(LimitDto::getCategoryId)
                .collect(Collectors.toSet());

        if (categoryIdsInRequest.isEmpty()) {
            return;
        }

        List<Category> existingCategories = categoryRepository.findByIdIn(
                categoryIdsInRequest.stream().toList());

        Set<Long> existingCategoryIds = existingCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        Set<Long> nonExistingIds = categoryIdsInRequest.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .collect(Collectors.toSet());

        if (!nonExistingIds.isEmpty()) {
            String errorMsg = String.format(
                    "Ошибка целостности: Несуществующие ID категорий: %s. Пожалуйста, убедитесь, что все категории существуют.", nonExistingIds);
            throw new IllegalArgumentException(errorMsg);
        }
    }
}