package com.teamfourpixels.dto;

import com.teamfourpixels.enums.OperationType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private OperationType type;
    private String externalId;
    private Instant transactionDate;
    private String description;
    private String merchantName;
    private String mcc;
    private CategoryDto category;
    private Boolean isIncome;

    private List<SplitPartDto> splits;
}