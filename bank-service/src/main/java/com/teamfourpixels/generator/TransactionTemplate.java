package com.teamfourpixels.generator;

import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@AllArgsConstructor
public class TransactionTemplate {

    final BigDecimal amount;
    final String merchantName;
    final String mcc;
    final String description;
    final Long bankCategoryId;
    final String bankCategoryName;
}