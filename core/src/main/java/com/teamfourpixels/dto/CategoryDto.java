package com.teamfourpixels.dto;

import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private boolean isSystem;
}