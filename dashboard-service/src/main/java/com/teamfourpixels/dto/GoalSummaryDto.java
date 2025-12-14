package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoalSummaryDto {
    private Long id;
    private String name;
    private BigDecimal saved;
    private BigDecimal target;
    private Integer progressPercent;
    private Long daysLeft;
    private BigDecimal recommendedMonthly;
}
