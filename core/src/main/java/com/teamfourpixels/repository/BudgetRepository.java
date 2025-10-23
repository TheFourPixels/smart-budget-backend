package com.teamfourpixels.repository;

import com.teamfourpixels.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndTime(Long userId, Integer time);

    void deleteByUserIdAndTime(Long userId, Integer time);
}