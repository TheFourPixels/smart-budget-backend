package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import org.mapstruct.*;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @Mapping(source = "year", target = "year")
    @Mapping(source = "month", target = "month")
    BudgetDto toDto(Budget entity);

    @Mapping(target = "budget", source = "budget")
    BudgetLimit toLimitEntity(LimitDto dto, Budget budget);

    default List<BudgetLimit> toLimitEntities(List<LimitDto> dtos, Budget budget) {
        return dtos.stream()
                .map(dto -> toLimitEntity(dto, budget))
                .collect(Collectors.toList());
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