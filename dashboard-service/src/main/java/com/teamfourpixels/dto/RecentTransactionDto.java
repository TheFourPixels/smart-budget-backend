package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class RecentTransactionDto {
    private String merchant;
    private String description;
    private BigDecimal amount;
    private Instant date;
    private String categoryName;
    private boolean isIncome;
}
