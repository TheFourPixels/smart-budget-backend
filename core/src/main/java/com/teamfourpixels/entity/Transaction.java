package com.teamfourpixels.entity;

import com.teamfourpixels.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant transactionTime;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OperationType type;

    @Column(length = 4)
    private String mcc;

    @Column(length = 255)
    private String merchant;

    @Column(nullable = false)
    private Long categoryId;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isSplit = false;

    @Column
    private Long originalTransactionId;

    @Column(name = "bank_transaction_ref_id", unique = true)
    private String bankTransactionRefId;
}