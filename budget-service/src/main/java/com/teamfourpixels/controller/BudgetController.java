package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.security.UserPrincipal;
import com.teamfourpixels.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetDto> createOrUpdateBudget(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(userPrincipal.getId(), request));
    }

    @GetMapping("/{time}")
    public ResponseEntity<BudgetDto> getBudget(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @PathVariable Integer time) {
        return ResponseEntity.ok(budgetService.getBudget(userPrincipal.getId(), time));
    }

    @DeleteMapping("/{time}")
    public ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                             @PathVariable Integer time) {
        budgetService.deleteBudget(userPrincipal.getId(), time);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{time}/dashboard")
    public ResponseEntity<DashboardDataDto> getDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @PathVariable Integer time) {
        DashboardDataDto data = new DashboardDataDto();
        BudgetDto budget = budgetService.getBudget(userPrincipal.getId(), time);

        data.setBudgetPlan(budget.getTotalIncome());
        data.setTotalSpent(BigDecimal.ZERO);
        data.setRemainingBudget(budget.getTotalIncome().subtract(data.getTotalSpent()));
        // data.setYear(time / 100);
        // data.setMonth(time % 100);
        // -----------------------------------------------------------------

        return ResponseEntity.ok(data);
    }
}
