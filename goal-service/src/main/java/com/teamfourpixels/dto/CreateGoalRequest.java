package com.teamfourpixels.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateGoalRequest {
    @NotBlank(message = "Название цели обязательно")
    private String name;

    @NotNull(message = "Целевая сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal targetAmount;

    @NotNull(message = "Дедлайн обязателен")
    @FutureOrPresent(message = "Дедлайн не может быть в прошлом")
    private LocalDate deadline;
}