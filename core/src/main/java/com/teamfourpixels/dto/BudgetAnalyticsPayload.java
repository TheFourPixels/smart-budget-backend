package com.teamfourpixels.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BudgetAnalyticsPayload {
    private BigDecimal totalIncome;
}
