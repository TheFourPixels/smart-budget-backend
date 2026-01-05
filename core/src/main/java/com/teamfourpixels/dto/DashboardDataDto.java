package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardDataDto {
    private Integer year;
    private Integer month;
    private BigDecimal budgetPlan;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;
    private List<CategoryStatDto> categoriesStats;
}
