package com.teamfourpixels.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryAnalyticsPayload {
    private String categoryName;
}
