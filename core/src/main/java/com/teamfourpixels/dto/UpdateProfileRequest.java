package com.teamfourpixels.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String avatarUrl;
}