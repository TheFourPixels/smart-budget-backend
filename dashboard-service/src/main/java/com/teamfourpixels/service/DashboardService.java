package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.enums.LimitType;
import com.teamfourpixels.enums.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WebClient budgetClient;
    private final WebClient transactionClient;
    private final WebClient goalClient;

    private static final Long USER_ID = 1L;
    private static final int RECENT_TX_COUNT = 5;
    private static final String USER_ID_HEADER = "X-User-Id";

    public DashboardResponse getDashboard(long userid, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        Instant start = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = ym.atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC).toInstant();

        String jwt = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        String authHeader = "Bearer " + jwt;

        BudgetDto budget = budgetClient.get()
                .uri("/budgets/{year}/{month}", year, month)
                .header(USER_ID_HEADER, USER_ID.toString())
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .onStatus(org.springframework.http.HttpStatusCode::is4xxClientError,
                        resp -> resp.statusCode().value() == 404
                                ? Mono.empty()
                                : Mono.error(new RuntimeException("Budget service error")))
                .bodyToMono(BudgetDto.class)
                .blockOptional()
                .orElse(new BudgetDto());

        List<TransactionDto> transactions = transactionClient.get()
                .uri(uri -> uri.path("/transactions")
                        .queryParam("page", 0)
                        .queryParam("size", 1000)
                        .queryParam("startDateMillis", start.toEpochMilli())
                        .queryParam("endDateMillis", end.toEpochMilli())
                        .build())
                .header(USER_ID_HEADER, USER_ID.toString())
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .collectList()
                .block();

        List<GoalDto> goals = goalClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/goals/active")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .header(USER_ID_HEADER, USER_ID.toString())
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(GoalDto.class)
                .collectList()
                .block();

        return buildResponse(year, month, budget, transactions != null ? transactions : Collections.emptyList(), goals);
    }

    private DashboardResponse buildResponse(int year, int month,
                                            BudgetDto budget,
                                            List<TransactionDto> transactions,
                                            List<GoalDto> goals) {

        DashboardResponse resp = new DashboardResponse();
        resp.setYear(year);
        resp.setMonth(month);

        BigDecimal totalIncome = Optional.ofNullable(budget.getTotalIncome()).orElse(BigDecimal.ZERO);
        BigDecimal totalSpent = transactions.stream()
                .filter(t -> t.getType() == OperationType.EXPENSE)
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resp.setTotalIncome(totalIncome);
        resp.setTotalSpent(totalSpent);
        resp.setRemainingBudget(totalIncome.subtract(totalSpent));

        resp.setCategoryStats(calculateCategoryStats(budget, transactions));
        resp.setRecentTransactions(getRecentTransactions(transactions));

        resp.setActiveGoals(goals != null ? goals.stream()
                .map(this::toGoalSummary)
                .limit(5)
                .toList() : List.of());

        return resp;
    }

    private List<CategoryStatDto> calculateCategoryStats(BudgetDto budget, List<TransactionDto> txs) {
        Map<Long, BigDecimal> spentByCategory = new HashMap<>();

        for (TransactionDto t : txs) {
            if (t.getType() == OperationType.EXPENSE) {
                Long catId = t.getCategory() != null ? t.getCategory().getId() : 999L;

                if (catId == -1L && t.getSplits() != null && !t.getSplits().isEmpty()) {
                    for (SplitPartDto split : t.getSplits()) {
                        spentByCategory.merge(
                                split.getCategoryId(),
                                split.getAmount(),
                                BigDecimal::add
                        );
                    }
                } else {
                    spentByCategory.merge(
                            catId,
                            t.getAmount().abs(),
                            BigDecimal::add
                    );
                }
            }
        }

        if (budget.getLimits() == null || budget.getLimits().isEmpty()) {
            return List.of();
        }

        return budget.getLimits().stream()
                .map(limit -> {
                    CategoryStatDto stat = new CategoryStatDto();
                    stat.setCategoryId(limit.getCategoryId());
                    stat.setCategoryName(getCategoryName(limit.getCategoryId()));

                    BigDecimal limitValue = calculateLimitValue(limit, budget.getTotalIncome());
                    BigDecimal spent = spentByCategory.getOrDefault(limit.getCategoryId(), BigDecimal.ZERO);

                    stat.setLimit(limitValue);
                    stat.setSpent(spent);
                    int percent = limitValue.compareTo(BigDecimal.ZERO) > 0
                            ? spent.multiply(BigDecimal.valueOf(100))
                            .divide(limitValue, 0, RoundingMode.HALF_UP)
                            .intValue()
                            : 0;

                    stat.setProgressPercent(Math.min(percent, 200));
                    stat.setOverLimit(percent >= 100);
                    stat.setColor(percent < 80 ? "green" : percent < 100 ? "yellow" : "red");

                    return stat;
                })
                .sorted(Comparator.comparing(CategoryStatDto::getProgressPercent).reversed())
                .toList();
    }

    private BigDecimal calculateLimitValue(LimitDto limit, BigDecimal totalIncome) {
        return limit.getLimitType() == LimitType.PERCENT
                ? totalIncome.multiply(limit.getLimitValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : limit.getLimitValue();
    }

    private List<RecentTransactionDto> getRecentTransactions(List<TransactionDto> txs) {
        return txs.stream()
                .sorted(Comparator.comparing(TransactionDto::getTransactionDate).reversed())
                .limit(RECENT_TX_COUNT)
                .map(t -> {
                    RecentTransactionDto dto = new RecentTransactionDto();
                    dto.setMerchant(t.getMerchantName());
                    dto.setDescription(t.getDescription());
                    dto.setAmount(t.getAmount());
                    dto.setDate(t.getTransactionDate());
                    dto.setCategoryName(t.getCategory() != null ? t.getCategory().getName() : "—");
                    dto.setIncome(t.getType() == OperationType.INCOME);
                    return dto;
                })
                .toList();
    }

    private GoalSummaryDto toGoalSummary(GoalDto g) {
        GoalSummaryDto s = new GoalSummaryDto();
        s.setId(g.getId());
        s.setName(g.getName());
        s.setSaved(g.getSavedAmount());
        s.setTarget(g.getTargetAmount());

        int progress = g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? g.getSavedAmount().multiply(BigDecimal.valueOf(100))
                .divide(g.getTargetAmount(), 0, RoundingMode.HALF_UP)
                .intValue()
                : 0;
        s.setProgressPercent(progress);

        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), g.getDeadline());
        s.setDaysLeft(daysLeft > 0 ? daysLeft : 0L);

        long monthsLeft = ChronoUnit.MONTHS.between(LocalDate.now(), g.getDeadline());
        BigDecimal recommended = monthsLeft > 0
                ? g.getTargetAmount().subtract(g.getSavedAmount())
                .divide(BigDecimal.valueOf(monthsLeft), 0, RoundingMode.UP)
                : BigDecimal.ZERO;
        s.setRecommendedMonthly(recommended);

        return s;
    }

    private String getCategoryName(Long id) {
        return switch (id.intValue()) {
            case 1 -> "Продукты";
            case 2 -> "Связь/Интернет";
            case 3 -> "Одежда";
            case 10 -> "Рестораны";
            default -> "Другое";
        };
    }
}