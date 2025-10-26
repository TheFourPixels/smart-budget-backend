package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
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

    private BudgetRepository budgetRepository;
    private BudgetMapper budgetMapper;

    public BudgetServiceImpl(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    @Transactional
    public BudgetDto createOrUpdateBudget(Long userId, CreateBudgetRequest request) {

        BigDecimal percentSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == BudgetLimit.LimitType.PERCENT)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (percentSum.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Сумма процентов лимитов должна равняться 100");
        }

        BigDecimal amountSum = request.getLimits().stream()
                .filter(l -> l.getLimitType() == BudgetLimit.LimitType.SUM)
                .map(LimitDto::getLimitValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amountSum.compareTo(request.getTotalIncome()) > 0) {
            throw new IllegalArgumentException("Сумма абсолютных лимитов не должна превышать общий доход");
        }

        Budget budget = budgetRepository
                .findByUserIdAndTime(userId, request.getTime())
                .orElse(Budget.builder().build());

        budget.setUserId(userId);
        budget.setTime(request.getTime());
        budget.setTotalIncome(request.getTotalIncome());

        budget.getLimits().clear();
        List<BudgetLimit> newLimits = budgetMapper.toLimitEntities(request.getLimits(), budget);
        budget.getLimits().addAll(newLimits);

        return budgetMapper.toDto(budgetRepository.save(budget));
    }

    @Override
    public BudgetDto getBudget(Long userId, Integer time) {
        return budgetRepository.findByUserIdAndTime(userId, time)
                .map(budgetMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Бюджет не найден"));
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Integer time) {
        budgetRepository.deleteByUserIdAndTime(userId, time);
    }
}