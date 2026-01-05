package com.teamfourpixels.entity;

import com.teamfourpixels.enums.LimitType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budget_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal limitValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LimitType limitType;

}