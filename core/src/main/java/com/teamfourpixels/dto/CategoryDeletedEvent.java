package com.teamfourpixels.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDeletedEvent {
    private Long categoryId;
    private Long userId;
}
