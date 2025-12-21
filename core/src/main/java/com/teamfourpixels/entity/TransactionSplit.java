package com.teamfourpixels.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transaction_splits")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionSplit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private Long categoryId;
    private BigDecimal amount;
    private String description;
}