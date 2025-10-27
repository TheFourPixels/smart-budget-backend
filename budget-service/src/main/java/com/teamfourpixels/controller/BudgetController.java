package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private static final Long DUMMY_USER_ID = 1L;

    @PostMapping("/{year}/{month}")
    public ResponseEntity<BudgetDto> createOrUpdateBudget(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @RequestBody CreateBudgetRequest request) {
        System.out.println("Received year: " + year + ", month: " + month + ", totalIncome: " + request.getTotalIncome());
        System.out.println("Limits: " + request.getLimits());
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(DUMMY_USER_ID, year, month, request));
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<BudgetDto> getBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        Integer time = convertToTime(year, month);
        return ResponseEntity.ok(budgetService.getBudget(DUMMY_USER_ID, year, month));
    }

    @DeleteMapping("/{year}/{month}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        Integer time = convertToTime(year, month);
        budgetService.deleteBudget(DUMMY_USER_ID, year, month);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{year}/{month}/dashboard")
    public ResponseEntity<DashboardDataDto> getDashboard(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        Integer time = convertToTime(year, month);
        DashboardDataDto data = new DashboardDataDto();
        BudgetDto budget = budgetService.getBudget(DUMMY_USER_ID, year, month);

        data.setBudgetPlan(budget.getTotalIncome());
        data.setTotalSpent(BigDecimal.ZERO);
        data.setRemainingBudget(budget.getTotalIncome().subtract(data.getTotalSpent()));
        data.setYear(year);
        data.setMonth(month);

        return ResponseEntity.ok(data);
    }

    private Integer convertToTime(Integer year, Integer month) {
        if (year == null || month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Некорректный год или месяц.");
        }
        return year * 100 + month;
    }
}