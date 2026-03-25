package com.teamfourpixels.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetLimitEvent {
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal currentSpent;
    private int percentage;
}