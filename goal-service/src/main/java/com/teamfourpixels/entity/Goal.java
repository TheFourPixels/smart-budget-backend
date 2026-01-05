package com.teamfourpixels.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "saved_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal savedAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}
