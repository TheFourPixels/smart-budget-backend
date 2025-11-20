package com.teamfourpixels.generator;

import java.math.BigDecimal;

public record TransactionTemplate(
        BigDecimal amount,
        String merchantName,
        String mcc,
        String description,
        Long bankCategoryId,
        String bankCategoryName) {

}