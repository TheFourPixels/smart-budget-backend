package com.teamfourpixels.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class GoalAnalyticsPayload {
    private Long goalId;
    private BigDecimal targetAmount;
}
