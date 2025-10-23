package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BudgetDto {
    private Long id;
    private Integer time;
    private BigDecimal totalIncome;
    private List<LimitDto> limits;
}