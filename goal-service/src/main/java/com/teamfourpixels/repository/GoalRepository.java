package com.teamfourpixels.repository;

import com.teamfourpixels.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findAllByUserId(Long userId);
    void deleteByUserIdAndId(Long userId, Long id);
}