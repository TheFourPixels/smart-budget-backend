package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.entity.Goal;
import org.mapstruct.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(
        componentModel = "spring",
        imports = {
                LocalDate.class,
                ChronoUnit.class
        }
)
public interface GoalMapper {

    @Mapping(target = "progressPercent", expression = "java(calculateProgress(entity))")
    @Mapping(target = "daysLeft", expression = "java(calculateDaysLeft(entity))")
    @Mapping(target = "recommendedMonthly", expression = "java(calculateRecommendedMonthly(entity))")
    GoalDto toDto(Goal entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "savedAmount", constant = "0")

    @Mapping(target = "createdAt", expression = "java(LocalDate.now())")
    Goal toEntity(CreateGoalRequest req);

    default Integer calculateProgress(Goal g) {
        if (g.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) return 0;
        return g.getSavedAmount()
                .divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }

    default Long calculateDaysLeft(Goal g) {
        return ChronoUnit.DAYS.between(LocalDate.now(), g.getDeadline());
    }

    default BigDecimal calculateRecommendedMonthly(Goal g) {
        long months = ChronoUnit.MONTHS.between(LocalDate.now(), g.getDeadline());
        if (months <= 0) return BigDecimal.ZERO;
        return g.getTargetAmount()
                .subtract(g.getSavedAmount())
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "savedAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(CreateGoalRequest dto, @MappingTarget Goal entity);
}