package com.teamfourpixels.service.classification;

import com.teamfourpixels.entity.CategorizationRule;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.repository.CategorizationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomRuleClassificationStrategy implements ClassificationStrategy {
    private final com.teamfourpixels.service.StrategyPriorityService strategyPriorityService;

    private final CategorizationRuleRepository repository;

    @Override
    public Optional<Long> classify(Transaction transaction) {
        List<CategorizationRule> rules = repository.findByUserId(transaction.getUserId());
        String merchant = transaction.getMerchant() == null ? "" : transaction.getMerchant();
        String description = transaction.getDescription() == null ? "" : transaction.getDescription();
        String searchText = (merchant + " " + description).toLowerCase();

        for (CategorizationRule rule : rules) {
            if (searchText.contains(rule.getKeyword().toLowerCase())) {
                return Optional.of(rule.getCategoryId());
            }
        }
        return Optional.empty();
    }

    @Override
    public int getPriority(Long userId) { return strategyPriorityService.getPriority(userId, this.getClass().getSimpleName(), 1); }
}