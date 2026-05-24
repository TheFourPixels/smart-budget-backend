package com.teamfourpixels.repository;
import com.teamfourpixels.entity.StrategyPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StrategyPriorityRepository extends JpaRepository<StrategyPriority, Long> {
    Optional<StrategyPriority> findByUserIdAndStrategyName(Long userId, String strategyName);
    List<StrategyPriority> findAllByUserId(Long userId);
}
