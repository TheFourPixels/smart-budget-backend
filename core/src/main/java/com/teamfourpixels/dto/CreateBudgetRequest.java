package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Запрос на создание или обновление бюджета")
public class CreateBudgetRequest {

    @NotNull
    @Min(2000)
    @Max(2100)
    @Schema(description = "Год", example = "2025")
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    @Schema(description = "Месяц (1-12)", example = "12")
    private Integer month;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Общий доход на месяц", example = "150000.00")
    private BigDecimal totalIncome;

    @Schema(description = "Лимит на траты (если меньше дохода)", example = "100000.00")
    private BigDecimal spendingLimit;

    @NotNull
    @Schema(description = "Список лимитов по категориям")
    private List<LimitDto> limits;
}