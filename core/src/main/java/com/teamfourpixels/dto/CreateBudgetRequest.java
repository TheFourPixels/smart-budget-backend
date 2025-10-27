package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateBudgetRequest {
    private Integer year;
    private Integer month;
    private BigDecimal totalIncome;
    private List<LimitDto> limits;
}