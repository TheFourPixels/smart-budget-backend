package com.teamfourpixels.service;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.entity.Goal;
import com.teamfourpixels.mapper.GoalMapper;
import com.teamfourpixels.repository.GoalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository repository;
    private final GoalMapper mapper;

    private final WebClient budgetClient;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final Long DUMMY_USER_ID = 1L;

    private void checkBudgetExists(Long userId, int year, int month) {
        try {
            budgetClient.get()
                    .uri("/budgets/{year}/{month}", year, month)
                    .header(USER_ID_HEADER, userId.toString())
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND,
                            response -> Mono.error(new IllegalArgumentException(
                                    String.format("Бюджет на %d/%d не найден. Создание цели невозможно.", month, year)
                            ))
                    )
                    .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Ошибка Budget Service")))
                    .toBodilessEntity()
                    .block();
        } catch (RuntimeException e) {
            throw e;
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

    @Transactional
    public GoalDto create(Long userId, CreateGoalRequest req) {
        YearMonth currentMonth = YearMonth.now();
        checkBudgetExists(userId, currentMonth.getYear(), currentMonth.getMonthValue());

        Goal goal = mapper.toEntity(req);
        goal.setUserId(userId);
        return mapper.toDto(repository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalDto> listActive(Long userId, int year, int month) {
        List<Goal> allUserGoals = repository.findAllByUserId(userId);

        return filterByCreationMonth(allUserGoals, year, month).stream()
                .filter(g -> g.getSavedAmount().compareTo(g.getTargetAmount()) < 0)
                .filter(g -> g.getDeadline().isAfter(LocalDate.now()) || g.getDeadline().isEqual(LocalDate.now()))
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GoalDto> listCompleted(Long userId, int year, int month) {
        List<Goal> allUserGoals = repository.findAllByUserId(userId);

        return filterByCreationMonth(allUserGoals, year, month).stream()
                .filter(g -> g.getSavedAmount().compareTo(g.getTargetAmount()) >= 0 ||
                        g.getDeadline().isBefore(LocalDate.now()))
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalDto get(Long userId, Long id) {
        return repository.findById(id)
                .filter(g -> g.getUserId().equals(userId))
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Цель не найдена"));
    }

    @Transactional
    public GoalDto contribute(Long userId, Long id, BigDecimal amount) {
        Goal goal = getByIdAndUser(id, userId);

        goal.setSavedAmount(goal.getSavedAmount().add(amount));
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) > 0) {
            goal.setSavedAmount(goal.getTargetAmount());
        }
        return mapper.toDto(repository.save(goal));
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

        return mapper.toDto(repository.save(goal));
    }
}