package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Запрос на разделение транзакции")
public class SplitTransactionRequest {

    @NotEmpty
    @Valid
    @Schema(description = "Список частей, на которые делится транзакция")
    private List<SplitPartRequest> splits;

    @Data
    public static class SplitPartRequest {
        @NotNull
        @Schema(description = "ID категории для части суммы", example = "2")
        private Long categoryId;

        @NotNull
        @Schema(description = "Сумма части", example = "500.00")
        private BigDecimal amount;

        @Schema(description = "Описание части", example = "Покупка молока")
        private String description;
    }
}