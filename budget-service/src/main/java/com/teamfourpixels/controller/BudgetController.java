package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BudgetDto> createOrUpdateBudget(
            @Valid @RequestBody CreateBudgetRequest request) {

        log.debug("Creating budget for {}/{}, totalIncome: {}, limits: {}",
                request.getYear(), request.getMonth(),
                request.getTotalIncome(), request.getLimits());

        BudgetDto budget = budgetService.createOrUpdateBudget(
                DUMMY_USER_ID,
                request.getYear(),
                request.getMonth(),
                request
        );

        return ResponseEntity.ok(budget);
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<BudgetDto> getBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        return ResponseEntity.ok(budgetService.getBudget(DUMMY_USER_ID, year, month));
    }

    @DeleteMapping("/{year}/{month}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        budgetService.deleteBudget(DUMMY_USER_ID, year, month);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{year}/{month}/dashboard")
    public ResponseEntity<DashboardDataDto> getDashboard(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        DashboardDataDto data = new DashboardDataDto();
        BudgetDto budget = budgetService.getBudget(DUMMY_USER_ID, year, month);

        data.setBudgetPlan(budget.getTotalIncome());
        data.setTotalSpent(BigDecimal.ZERO);
        data.setRemainingBudget(budget.getTotalIncome().subtract(data.getTotalSpent()));
        data.setYear(year);
        data.setMonth(month);

        return ResponseEntity.ok(data);
    }
}