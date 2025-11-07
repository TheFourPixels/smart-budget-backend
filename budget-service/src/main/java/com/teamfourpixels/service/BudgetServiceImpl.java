package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import com.teamfourpixels.enums.LimitType;
import com.teamfourpixels.mapper.BudgetMapper;
import com.teamfourpixels.repository.BudgetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    @Override
    @Transactional
    public BudgetDto createOrUpdateBudget(Long userId, Integer year, Integer month, CreateBudgetRequest request) {
        validatePercentLimits(request);
        validateAmountLimits(request);

        Budget budget = budgetRepository
                .findByUserIdAndYearAndMonth(userId, year, month)
                .orElse(Budget.builder().build());

        budgetMapper.updateBudgetFields(budget, request, userId, year, month);

        budget.getLimits().clear();

        List<BudgetLimit> newLimits = budgetMapper.toLimitEntities(request.getLimits(), budget);
        budget.getLimits().addAll(newLimits);

        return budgetMapper.toDto(budgetRepository.save(budget));
    }

    private void validatePercentLimits(CreateBudgetRequest request) {
        BigDecimal percentSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.PERCENT)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (percentSum.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Сумма процентов должна быть 100");
        }
    }

    private void validateAmountLimits(CreateBudgetRequest request) {
        BigDecimal amountSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == LimitType.SUM)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amountSum.compareTo(request.getTotalIncome()) > 0) {
            throw new IllegalArgumentException("Сумма лимитов не должна превышать доход");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDto getBudget(Long userId, Integer year, Integer month) {
        return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(budgetMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Бюджет не найден"));
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Integer year, Integer month) {
        budgetRepository.deleteByUserIdAndYearAndMonth(userId, year, month);
    }
}