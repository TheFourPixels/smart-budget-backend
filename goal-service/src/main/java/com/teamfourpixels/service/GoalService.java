package com.teamfourpixels.service;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.entity.Goal;
import com.teamfourpixels.mapper.GoalMapper;
import com.teamfourpixels.repository.GoalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository repository;
    private final GoalMapper mapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${service.budget.url}")
    private String budgetServiceUrl;

    private final AnalyticsService analyticsService;

    private static final String USER_ID_HEADER = "X-User-Id";

    private Integer calculateProgress(Goal g) {
        if (g.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) return 0;
        return g.getSavedAmount()
                .divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }

    private Long calculateDaysLeft(Goal g) {
        return ChronoUnit.DAYS.between(LocalDate.now(), g.getDeadline());
    }

    private BigDecimal calculateRecommendedMonthly(Goal g) {
        long months = ChronoUnit.MONTHS.between(LocalDate.now(), g.getDeadline());
        if (months <= 0) return BigDecimal.ZERO;
        return g.getTargetAmount()
                .subtract(g.getSavedAmount())
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }

    private GoalDto addCalculatedFields(Goal entity) {
        GoalDto dto = mapper.toDto(entity);
        dto.setProgressPercent(calculateProgress(entity));
        dto.setDaysLeft(calculateDaysLeft(entity));
        dto.setRecommendedMonthly(calculateRecommendedMonthly(entity));
        return dto;
    }

    private void checkBudgetExists(Long userId, int year, int month) {
        String jwt = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        HttpHeaders headers = new HttpHeaders();
        headers.set(USER_ID_HEADER, userId.toString());
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            restTemplate.exchange(
                    budgetServiceUrl + "/api/v1/budgets/" + year + "/" + month,
                    HttpMethod.GET,
                    entity,
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(String.format("Бюджет на %d/%d не найден. Создание цели невозможно.", month, year));
        } catch (Exception e) {
            log.error("Error connecting to Budget Service at {}: {}", budgetServiceUrl, e.getMessage());
            throw new RuntimeException("Ошибка Budget Service", e);
        }
    }

    private List<Goal> filterByCreationMonth(List<Goal> goals, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return goals.stream()
                .filter(g -> YearMonth.from(g.getCreatedAt()).equals(ym))
                .toList();
    }

    private Goal getByIdAndUser(Long id, Long userId) {
        return repository.findById(id)
                .filter(g -> g.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Цель не найдена"));
    }

    public GoalDto create(Long userId, CreateGoalRequest req) {
        YearMonth currentMonth = YearMonth.now();
        checkBudgetExists(userId, currentMonth.getYear(), currentMonth.getMonthValue());

        Goal goal = mapper.toEntity(req);
        goal.setUserId(userId);
        Goal savedGoal = repository.save(goal);

        analyticsService.sendEvent(userId, "GOAL_CREATED",
                String.format("{\"goalId\":%d, \"targetAmount\":%s}", savedGoal.getId(), savedGoal.getTargetAmount()));

        return addCalculatedFields(savedGoal);
    }

    @Transactional(readOnly = true)
    public List<GoalDto> listActive(Long userId, int year, int month) {
        List<Goal> allUserGoals = repository.findAllByUserId(userId);

        return filterByCreationMonth(allUserGoals, year, month).stream()
                .filter(g -> g.getSavedAmount().compareTo(g.getTargetAmount()) < 0)
                .filter(g -> g.getDeadline().isAfter(LocalDate.now()) || g.getDeadline().isEqual(LocalDate.now()))
                .map(this::addCalculatedFields)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GoalDto> listCompleted(Long userId, int year, int month) {
        List<Goal> allUserGoals = repository.findAllByUserId(userId);

        return filterByCreationMonth(allUserGoals, year, month).stream()
                .filter(g -> g.getSavedAmount().compareTo(g.getTargetAmount()) >= 0 ||
                        g.getDeadline().isBefore(LocalDate.now()))
                .map(this::addCalculatedFields)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalDto get(Long userId, Long id) {
        return repository.findById(id)
                .filter(g -> g.getUserId().equals(userId))
                .map(this::addCalculatedFields)
                .orElseThrow(() -> new EntityNotFoundException("Цель не найдена"));
    }

    @Transactional
    public GoalDto contribute(Long userId, Long id, BigDecimal amount) {
        Goal goal = getByIdAndUser(id, userId);

        BigDecimal amountBefore = goal.getSavedAmount();
        BigDecimal target = goal.getTargetAmount();

        goal.setSavedAmount(amountBefore.add(amount));
        if (goal.getSavedAmount().compareTo(target) > 0) {
            goal.setSavedAmount(target);
        }

        Goal savedGoal = repository.save(goal);

        if (amountBefore.compareTo(target) < 0 && savedGoal.getSavedAmount().compareTo(target) >= 0) {
            analyticsService.sendEvent(userId, "GOAL_ACHIEVED", "{\"goalId\":" + id + "}");
            log.info("Пользователь {} достиг финансовой цели {}", userId, id);
        }

        return addCalculatedFields(savedGoal);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Goal goalToDelete = getByIdAndUser(id, userId);
        repository.delete(goalToDelete);
    }

    @Transactional
    public GoalDto update(Long userId, Long id, CreateGoalRequest req) {
        Goal goal = getByIdAndUser(id, userId);

        mapper.updateEntity(req, goal);

        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) > 0) {
            goal.setSavedAmount(goal.getTargetAmount());
        }

        Goal savedGoal = repository.save(goal);
        return addCalculatedFields(savedGoal);
    }
}
