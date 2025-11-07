package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import org.mapstruct.*;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    BudgetDto toDto(Budget entity);

    default List<BudgetLimit> toLimitEntities(List<LimitDto> limitDtos, Budget budget) {
        return limitDtos.stream()
                .map(dto -> BudgetLimit.builder()
                        .budget(budget)
                        .categoryId(dto.getCategoryId())
                        .limitValue(dto.getLimitValue())
                        .limitType(dto.getLimitType())
                        .build())
                .collect(Collectors.toList());
    }

    default void updateBudgetFields(Budget budget, CreateBudgetRequest request, Long userId, Integer year, Integer month) {
        budget.setUserId(userId);
        budget.setYear(year);
        budget.setMonth(month);
        budget.setTotalIncome(request.getTotalIncome());
    }
}