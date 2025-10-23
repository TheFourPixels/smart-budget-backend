package com.teamfourpixels.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
// Предполагаем, что system_categories и personal_categories объединены
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean isSystem;
}
