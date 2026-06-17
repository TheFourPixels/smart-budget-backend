package com.teamfourpixels.dto;

import com.teamfourpixels.enums.OperationType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransactionAnalyticsPayload {
    private BigDecimal amount;
    private OperationType type;
    private String source;
    private Long categoryId;
}
