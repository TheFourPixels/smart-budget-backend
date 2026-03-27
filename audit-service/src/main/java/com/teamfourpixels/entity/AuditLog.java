package com.teamfourpixels.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private String action;
    private Long userId;
    private Long transactionId;
    private Long oldCategoryId;
    private Long newCategoryId;
    private LocalDateTime timestamp;
    private LocalDateTime processedAt;
}