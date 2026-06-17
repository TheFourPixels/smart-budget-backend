package com.teamfourpixels.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdatedEvent {
    private Long id;
    private Long userId;
    private Long oldCategoryId;
    private Long newCategoryId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
