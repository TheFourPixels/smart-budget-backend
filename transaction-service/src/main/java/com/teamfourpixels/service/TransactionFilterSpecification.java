package com.teamfourpixels.service;

import com.teamfourpixels.entity.Transaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

@RequiredArgsConstructor
public class TransactionFilterSpecification implements Specification<Transaction> {
    private final Long userId;
    private final Long categoryId;
    private final String query;
    private final Instant start;
    private final Instant end;

    @Override
    public Predicate toPredicate(Root<Transaction> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        predicate = cb.and(predicate, cb.equal(root.get("userId"), userId));

        if (start != null && end != null) {
            predicate = cb.and(predicate, cb.between(root.get("transactionTime"), start, end));
        } else if (start != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("transactionTime"), start));
        } else if (end != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("transactionTime"), end));
        }

        if (categoryId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("categoryId"), categoryId));
        }

        if (this.query != null && !this.query.isBlank()) {
            String pattern = "%" + this.query.toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("merchant")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
            predicate = cb.and(predicate, searchPredicate);
        }

        return predicate;
    }
}