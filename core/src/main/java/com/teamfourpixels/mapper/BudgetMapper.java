package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import org.mapstruct.*;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    BudgetDto toDto(Budget entity);

    @Mapping(target = "totalIncome", source = "request.totalIncome")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "limits", ignore = true)
    void updateBudgetFields(@MappingTarget Budget budget, CreateBudgetRequest request, Long userId, Integer year, Integer month);

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
}