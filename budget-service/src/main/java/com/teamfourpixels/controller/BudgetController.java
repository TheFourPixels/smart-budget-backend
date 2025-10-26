package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.security.UserPrincipal;
import com.teamfourpixels.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/v1/budgets")
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class BudgetController {

    private BudgetService budgetService;

    private Integer convertToTime(Integer year, Integer month) {
        if (year == null || month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Некорректный год или месяц.");
        }
        return year * 100 + month;
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createOrUpdateBudget(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(userPrincipal.getId(), request));
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<BudgetDto> getBudget(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable Integer year,
                                               @PathVariable Integer month) {
        Integer time = convertToTime(year, month);
        return ResponseEntity.ok(budgetService.getBudget(userPrincipal.getId(), time));
    }

    @DeleteMapping("/{year}/{month}")
    public ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                             @PathVariable Integer year,
                                             @PathVariable Integer month) {
        Integer time = convertToTime(year, month);
        budgetService.deleteBudget(userPrincipal.getId(), time);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{year}/{month}/dashboard")
    public ResponseEntity<DashboardDataDto> getDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @PathVariable Integer year,
                                                         @PathVariable Integer month) {
        Integer time = convertToTime(year, month);
        DashboardDataDto data = new DashboardDataDto();
        BudgetDto budget = budgetService.getBudget(userPrincipal.getId(), time);

        data.setBudgetPlan(budget.getTotalIncome());
        data.setTotalSpent(BigDecimal.ZERO);
        data.setRemainingBudget(budget.getTotalIncome().subtract(data.getTotalSpent()));

        data.setYear(year);
        data.setMonth(month);

        return ResponseEntity.ok(data);
    }
}