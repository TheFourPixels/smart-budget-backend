package com.teamfourpixels.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorization_rules")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CategorizationRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String keyword;
    private Long categoryId;
}