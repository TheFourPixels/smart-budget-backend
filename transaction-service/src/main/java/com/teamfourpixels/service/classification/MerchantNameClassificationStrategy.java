package com.teamfourpixels.service.classification;

import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.enums.OperationType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MerchantNameClassificationStrategy implements ClassificationStrategy {

    private static final Long SALARY_CATEGORY_ID = 1L;

    @Override
    public Optional<Long> classify(Transaction transaction) {
        String merchant = transaction.getMerchant();

        if (transaction.getType() == OperationType.INCOME &&
                merchant != null &&
                merchant.toLowerCase().contains("зарплата")) {

            return Optional.of(SALARY_CATEGORY_ID);
        }
        return Optional.empty();
    }
}