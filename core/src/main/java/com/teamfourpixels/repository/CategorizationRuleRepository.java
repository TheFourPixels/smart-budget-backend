package com.teamfourpixels.repository;

import com.teamfourpixels.entity.CategorizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategorizationRuleRepository extends JpaRepository<CategorizationRule, Long> {

    List<CategorizationRule> findByUserId(Long userId);
}