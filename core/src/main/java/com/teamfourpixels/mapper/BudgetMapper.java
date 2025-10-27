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
}