package com.teamfourpixels.service;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.entity.Goal;
import com.teamfourpixels.mapper.GoalMapper;
import com.teamfourpixels.repository.GoalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository repository;
    private final GoalMapper mapper;

    private static final Long DUMMY_USER_ID = 1L;

    @Transactional
    public GoalDto create(Long userId, CreateGoalRequest req) {
        Goal goal = mapper.toEntity(req);
        goal.setUserId(userId);
        return mapper.toDto(repository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalDto> list(Long userId) {
        return repository.findAllByUserId(userId).stream()
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
        Goal goal = repository.findById(id)
                .filter(g -> g.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Цель не найдена"));

        goal.setSavedAmount(goal.getSavedAmount().add(amount));
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) > 0) {
            goal.setSavedAmount(goal.getTargetAmount());
        }
        return mapper.toDto(repository.save(goal));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        repository.deleteByUserIdAndId(userId, id);
    }
}
