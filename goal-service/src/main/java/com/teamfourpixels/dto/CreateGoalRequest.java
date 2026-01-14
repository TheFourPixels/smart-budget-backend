package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Запрос на создание финансовой цели")
public class CreateGoalRequest {

        @NotBlank
        @Schema(description = "Название цели", example = "Новый ноутбук")
        private String name;

        @NotNull
        @DecimalMin("0.0")
        @Schema(description = "Целевая сумма", example = "150000.00")
        private BigDecimal targetAmount;

        @NotNull
        @Future
        @Schema(description = "Дата завершения (дедлайн)", example = "2026-06-01")
        private LocalDate deadline;
}