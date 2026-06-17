package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.dto.GoalSummaryDto;
import com.teamfourpixels.dto.TransactionDto;
import com.teamfourpixels.dto.RecentTransactionDto;
import com.teamfourpixels.enums.OperationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public abstract class DashboardMapper {

    @Mapping(target = "saved", source = "savedAmount")
    @Mapping(target = "target", source = "targetAmount")
    @Mapping(target = "progressPercent", expression = "java(calculateProgress(dto))")
    @Mapping(target = "daysLeft", expression = "java(calculateDaysLeft(dto))")
    @Mapping(target = "recommendedMonthly", expression = "java(calculateRecommendedMonthly(dto))")
    public abstract GoalSummaryDto toGoalSummary(GoalDto dto);

    @Mapping(target = "merchant", source = "merchantName")
    @Mapping(target = "date", source = "transactionDate")
    @Mapping(target = "categoryName", expression = "java(dto.getCategory() != null ? dto.getCategory().getName() : \"—\")")
    @Mapping(target = "income", expression = "java(dto.getType() == com.teamfourpixels.enums.OperationType.INCOME)")
    public abstract RecentTransactionDto toRecentDto(TransactionDto dto);

    protected int calculateProgress(GoalDto dto) {
        if (dto.getTargetAmount() != null && dto.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            return dto.getSavedAmount().multiply(BigDecimal.valueOf(100))
                    .divide(dto.getTargetAmount(), 0, RoundingMode.HALF_UP)
                    .intValue();
        }
        return 0;
    }

    protected long calculateDaysLeft(GoalDto dto) {
        if (dto.getDeadline() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), dto.getDeadline());
            return days > 0 ? days : 0L;
        }
        return 0L;
    }

    protected BigDecimal calculateRecommendedMonthly(GoalDto dto) {
        if (dto.getDeadline() != null) {
            long monthsLeft = ChronoUnit.MONTHS.between(LocalDate.now(), dto.getDeadline());
            BigDecimal remaining = dto.getTargetAmount().subtract(dto.getSavedAmount());
            if (remaining.signum() > 0 && monthsLeft > 0) {
                return remaining.divide(BigDecimal.valueOf(monthsLeft), 0, RoundingMode.UP);
            } else if (remaining.signum() > 0) {
                return remaining;
            }
        }
        return BigDecimal.ZERO;
    }
}
