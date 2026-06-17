package com.teamfourpixels.service;
import com.teamfourpixels.entity.StrategyPriority;
import com.teamfourpixels.repository.StrategyPriorityRepository;
import com.teamfourpixels.dto.StrategyPriorityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyPriorityService {
    private final StrategyPriorityRepository repository;

    public int getPriority(Long userId, String strategyName, int defaultPriority) {
        return repository.findByUserIdAndStrategyName(userId, strategyName)
                .map(StrategyPriority::getPriority)
                .orElse(defaultPriority);
    }

    @Transactional
    public void updatePriorities(Long userId, List<StrategyPriorityDto> priorities) {
        for (StrategyPriorityDto p : priorities) {
            StrategyPriority sp = repository.findByUserIdAndStrategyName(userId, p.getStrategyName())
                    .orElse(StrategyPriority.builder().userId(userId).strategyName(p.getStrategyName()).build());
            sp.setPriority(p.getPriority());
            repository.save(sp);
        }
    }
    
    @Transactional(readOnly = true)
    public List<StrategyPriorityDto> getPriorities(Long userId) {
        return repository.findAllByUserId(userId).stream().map(sp -> {
            StrategyPriorityDto dto = new StrategyPriorityDto();
            dto.setStrategyName(sp.getStrategyName());
            dto.setPriority(sp.getPriority());
            return dto;
        }).toList();
    }
}
