package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryStatDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal limit;
    private BigDecimal spent;
    private Integer progressPercent;
    private boolean isOverLimit;
    private String color;
}