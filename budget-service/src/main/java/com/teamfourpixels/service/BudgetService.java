package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;

public interface BudgetService {
    BudgetDto createOrUpdateBudget(Long userId, CreateBudgetRequest request);

    BudgetDto getBudget(Long userId, Integer time);
    void deleteBudget(Long userId, Integer time);
}
