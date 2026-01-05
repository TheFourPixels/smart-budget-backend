package com.teamfourpixels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTotalSpentDto {
    private Long categoryId;
    private BigDecimal totalSpent;
}