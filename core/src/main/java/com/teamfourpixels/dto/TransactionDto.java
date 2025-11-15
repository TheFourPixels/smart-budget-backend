package com.teamfourpixels.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private String external_id;
    private Instant transaction_date;
    private String description;
    private String merchant_name;
    private String mcc;
    private CategoryDto category;
    private Long parent_transaction_id;
}