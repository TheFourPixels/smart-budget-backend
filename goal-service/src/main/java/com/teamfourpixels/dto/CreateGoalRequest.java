package com.teamfourpixels.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalRequest(
        @NotBlank(message = "Название цели обязательно")
        String name,

        @NotNull(message = "Целевая сумма обязательна")
        @Positive(message = "Сумма должна быть положительной")
        BigDecimal targetAmount,

        @NotNull(message = "Дедлайн обязателен")
        @FutureOrPresent(message = "Дедлайн не может быть в прошлом")
        LocalDate deadline
) {}