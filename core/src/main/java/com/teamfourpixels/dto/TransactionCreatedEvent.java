package com.teamfourpixels.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreatedEvent {
    private Long transactionId;
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
}