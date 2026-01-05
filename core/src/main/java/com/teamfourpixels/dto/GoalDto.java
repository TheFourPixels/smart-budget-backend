package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalDto {
    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private LocalDate deadline;
    private LocalDate createdAt;
    private Integer progressPercent;
    private Long daysLeft;
    private BigDecimal recommendedMonthly;
}
