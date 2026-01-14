package com.teamfourpixels.dto;

import com.teamfourpixels.enums.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Schema(description = "Запрос на создание транзакции вручную")
public class CreateTransactionRequest {

    @NotNull
    @Schema(description = "Дата и время транзакции", example = "2025-12-05T10:00:00Z")
    private Instant transactionTime;

    @NotNull
    @Schema(description = "Сумма операции", example = "2500.50")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Тип операции (INCOME/EXPENSE)", example = "EXPENSE")
    private OperationType type;

    @Schema(description = "Название магазина/места", example = "Кафе 'Уют'")
    private String merchant;

    @NotNull
    @Schema(description = "ID категории", example = "3")
    private Long categoryId;

    @Schema(description = "Описание", example = "Ланч с коллегами")
    private String description;
}