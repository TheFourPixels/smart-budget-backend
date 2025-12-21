package com.teamfourpixels.service.classification;

import com.teamfourpixels.entity.CategorizationRule;
import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.repository.CategorizationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
public class CustomRuleClassificationStrategy implements ClassificationStrategy {

    private final CategorizationRuleRepository repository;

    @Override
    public Optional<Long> classify(Transaction transaction) {
        List<CategorizationRule> rules = repository.findByUserId(transaction.getUserId());

        String searchText = (transaction.getMerchant() + " " + transaction.getDescription()).toLowerCase();

        for (CategorizationRule rule : rules) {
            if (searchText.contains(rule.getKeyword().toLowerCase())) {
                return Optional.of(rule.getCategoryId());
            }
        }
        return Optional.empty();
    }
}