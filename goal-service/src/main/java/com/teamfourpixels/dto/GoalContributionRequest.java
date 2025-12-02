package com.teamfourpixels.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoalContributionRequest {
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}