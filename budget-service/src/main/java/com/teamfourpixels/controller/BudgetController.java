package com.teamfourpixels.controller;

import com.teamfourpixels.dto.BudgetDto;
import com.teamfourpixels.dto.CreateBudgetRequest;
import com.teamfourpixels.dto.DashboardDataDto;
import com.teamfourpixels.service.BudgetService;
import com.teamfourpixels.util.UserContext;
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

    @PostMapping
    public BudgetDto createOrUpdateBudget(@Valid @RequestBody CreateBudgetRequest request) {
        log.debug("Creating budget for user {}, {}/{}", UserContext.getUserId(), request.getYear(), request.getMonth());

        return budgetService.createOrUpdateBudget(
                UserContext.getUserId(),
                request.getYear(),
                request.getMonth(),
                request
        );
    }

    @GetMapping("/{year}/{month}")
    public BudgetDto getBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        return budgetService.getBudget(UserContext.getUserId(), year, month);
    }

    @DeleteMapping("/{year}/{month}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        budgetService.deleteBudget(UserContext.getUserId(), year, month);
    }

    @GetMapping("/{year}/{month}/dashboard")
    public DashboardDataDto getDashboard(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        BudgetDto budget = budgetService.getBudget(UserContext.getUserId(), year, month);
        DashboardDataDto data = new DashboardDataDto();
        data.setBudgetPlan(budget.getTotalIncome());
        data.setTotalSpent(BigDecimal.ZERO);
        data.setRemainingBudget(budget.getTotalIncome());
        data.setYear(year);
        data.setMonth(month);
        return data;
    }
}