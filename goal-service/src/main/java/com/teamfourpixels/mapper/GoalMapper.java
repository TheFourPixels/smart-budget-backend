package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.entity.Goal;
import org.mapstruct.*;
import java.time.LocalDate;

@Mapper(
        componentModel = "spring",
        imports = {
                LocalDate.class
        }
)
public interface GoalMapper {

    @Mapping(target = "progressPercent", ignore = true)
    @Mapping(target = "daysLeft", ignore = true)
    @Mapping(target = "recommendedMonthly", ignore = true)
    GoalDto toDto(Goal entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "savedAmount", constant = "0")
    @Mapping(target = "createdAt", expression = "java(LocalDate.now())")
    Goal toEntity(CreateGoalRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "savedAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(CreateGoalRequest dto, @MappingTarget Goal entity);
}