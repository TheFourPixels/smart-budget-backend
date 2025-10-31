package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    BudgetDto toDto(Budget entity);

    BudgetLimit toLimitEntity(LimitDto dto, Budget budget);

    default List<BudgetLimit> toLimitEntities(List<LimitDto> dtos, Budget budget) {
        return dtos.stream()
                .map(dto -> {
                    BudgetLimit limit = toLimitEntity(dto, budget);
                    limit.setBudget(budget);
                    return limit;
                })
                .toList();
    }

    default Budget toEntity(CreateBudgetRequest request, Budget existingBudget, Long userId) {
        existingBudget.setUserId(userId);
        existingBudget.setYear(request.getYear());
        existingBudget.setMonth(request.getMonth());
        existingBudget.setTotalIncome(request.getTotalIncome());

        existingBudget.getLimits().clear();
        existingBudget.getLimits().addAll(
                toLimitEntities(request.getLimits(), existingBudget)
        );

        return existingBudget;
    }
}