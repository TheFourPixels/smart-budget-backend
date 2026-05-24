package com.teamfourpixels.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoCategoryCorrectionPayload {
    private Long transactionId;
    private Long oldCat;
    private Long newCat;
}
