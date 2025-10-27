package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;

public interface BudgetService {
    BudgetDto createOrUpdateBudget(Long userId, Integer year, Integer month, CreateBudgetRequest request);
    BudgetDto getBudget(Long userId, Integer year, Integer month);
    void deleteBudget(Long userId, Integer year, Integer month);
}