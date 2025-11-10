package com.teamfourpixels.dto;

import com.teamfourpixels.enums.LimitType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LimitDto {
    private Long categoryId;
    private BigDecimal limitValue;
    private LimitType limitType;
}
