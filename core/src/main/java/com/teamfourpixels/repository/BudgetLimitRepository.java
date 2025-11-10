package com.teamfourpixels.repository;

import com.teamfourpixels.entity.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    List<BudgetLimit> findByBudgetId(Long budgetId);
}
