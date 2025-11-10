package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private static final Long DUMMY_USER_ID = 1L;

    @PostMapping
    public BudgetDto createOrUpdateBudget(
            @Valid @RequestBody CreateBudgetRequest request) {

        log.debug("Creating budget for {}/{}, totalIncome: {}, limits: {}",
                request.getYear(), request.getMonth(),
                request.getTotalIncome(), request.getLimits());

        return budgetService.createOrUpdateBudget(
                DUMMY_USER_ID,
                request.getYear(),
                request.getMonth(),
                request
        );
    }

    @GetMapping("/{year}/{month}")
    public BudgetDto getBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        return budgetService.getBudget(DUMMY_USER_ID, year, month);
    }

    @DeleteMapping("/{year}/{month}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        budgetService.deleteBudget(DUMMY_USER_ID, year, month);
    }

    @GetMapping("/{year}/{month}/dashboard")
    public DashboardDataDto getDashboard(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        BudgetDto budget = budgetService.getBudget(DUMMY_USER_ID, year, month);
        DashboardDataDto data = new DashboardDataDto();
        data.setBudgetPlan(budget.getTotalIncome());
        data.setTotalSpent(BigDecimal.ZERO);
        data.setRemainingBudget(budget.getTotalIncome().subtract(data.getTotalSpent()));
        data.setYear(year);
        data.setMonth(month);
        return data;
    }
}