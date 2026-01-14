package com.teamfourpixels.dto;

import com.teamfourpixels.enums.LimitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Лимит по категории")
public class LimitDto {

    @Schema(description = "ID категории", example = "1")
    private Long categoryId;

    @Schema(description = "Значение лимита", example = "50000.00")
    private BigDecimal limitValue;

    @Schema(description = "Тип лимита (SUM - сумма, PERCENT - процент)", example = "SUM")
    private LimitType limitType;
}