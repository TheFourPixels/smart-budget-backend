package com.teamfourpixels.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAnalyticsPayload {
    private String email;
    private String source;
}
