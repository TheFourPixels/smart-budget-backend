package com.teamfourpixels.service;

import com.teamfourpixels.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class TransactionSpecs {

    public static Specification<Transaction> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<Transaction> betweenDates(Instant start, Instant end) {
        return (root, query, cb) -> cb.between(root.get("transactionTime"), start, end);
    }

    public static Specification<Transaction> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<Transaction> merchantOrDescriptionContains(String searchText) {
        String pattern = "%" + searchText.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("merchant")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }
}