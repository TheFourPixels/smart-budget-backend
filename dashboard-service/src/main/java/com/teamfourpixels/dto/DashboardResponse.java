package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardResponse {
    private Integer year;
    private Integer month;

    private BigDecimal totalIncome;
    private BigDecimal spendingLimit;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;

    private List<CategoryStatDto> categoryStats;
    private List<RecentTransactionDto> recentTransactions;
    private List<GoalSummaryDto> activeGoals;
}