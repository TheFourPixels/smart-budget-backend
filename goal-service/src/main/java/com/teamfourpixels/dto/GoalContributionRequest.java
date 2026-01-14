package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос на внесение средств в цель")
public class GoalContributionRequest {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Сумма взноса", example = "5000.00")
    private BigDecimal amount;
}