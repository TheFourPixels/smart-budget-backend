package com.teamfourpixels.dto;

import com.teamfourpixels.enums.OperationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CreateTransactionRequest {
    private Instant transactionTime;
    private BigDecimal amount;
    private OperationType type;
    private String merchant;
    private String mcc;
    private Long categoryId;
    private String description;
}
