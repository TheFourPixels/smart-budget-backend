package com.teamfourpixels.service.classification;

import com.teamfourpixels.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@lombok.RequiredArgsConstructor
public class MccClassificationStrategy implements ClassificationStrategy {
    private final com.teamfourpixels.service.StrategyPriorityService strategyPriorityService;

    private static final Map<String, Long> MCC_TO_CATEGORY = Map.ofEntries(
            Map.entry("5411", 1L), Map.entry("5331", 1L), Map.entry("5441", 1L),
            Map.entry("5451", 1L), Map.entry("5499", 1L),
            Map.entry("5812", 10L), Map.entry("5814", 10L), Map.entry("5921", 10L),
            Map.entry("4900", 8L), Map.entry("4901", 8L),
            Map.entry("5541", 4L), Map.entry("5542", 4L), Map.entry("5983", 4L),
            Map.entry("5651", 3L), Map.entry("5621", 3L), Map.entry("5691", 3L),
            Map.entry("5641", 3L), Map.entry("5661", 3L)
    );

    @Override
    public Optional<Long> classify(Transaction transaction) {
        return Optional.ofNullable(transaction.getMcc())
                .map(MCC_TO_CATEGORY::get);
    }

    @Override
    public int getPriority(Long userId) { return strategyPriorityService.getPriority(userId, this.getClass().getSimpleName(), 3); }
}