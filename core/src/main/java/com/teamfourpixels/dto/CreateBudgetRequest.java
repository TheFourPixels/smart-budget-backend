package com.teamfourpixels.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateBudgetRequest {

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be >= 2000")
    @Max(value = 2100, message = "Year must be <= 2100")
    private Integer year;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be >= 1")
    @Max(value = 12, message = "Month must be <= 12")
    private Integer month;

    @NotNull(message = "Total income is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalIncome;

    @NotNull(message = "Limits list cannot be null")
    private List<LimitDto> limits;
}