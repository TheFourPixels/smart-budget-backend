package com.teamfourpixels.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "strategy_priorities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StrategyPriority {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String strategyName;
    private int priority;
}
