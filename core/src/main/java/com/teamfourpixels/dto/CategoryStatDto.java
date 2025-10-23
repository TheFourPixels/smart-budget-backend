package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryStatDto {
    private Long categoryId;
    private BigDecimal spentAmount;
    private BigDecimal budgetLimit;
    private boolean isOverLimit;
}