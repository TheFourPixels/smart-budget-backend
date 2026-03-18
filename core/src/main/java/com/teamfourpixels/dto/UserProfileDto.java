package com.teamfourpixels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private boolean pushEnabled;
    private boolean emailEnabled;
}