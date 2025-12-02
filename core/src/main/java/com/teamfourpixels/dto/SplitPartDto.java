package com.teamfourpixels.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SplitPartDto {
    private Long categoryId;
    private BigDecimal amount;
    private String description;
}