package com.teamfourpixels.repository;

import com.teamfourpixels.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByUserIdAndTransactionTimeBetweenOrderByTransactionTimeDesc(Long userId, Instant start, Instant end);
    Optional<Transaction> findByUserIdAndBankTransactionRefId(Long userId, String bankTransactionRefId);
    List<Transaction> findByOriginalTransactionId(Long originalTransactionId);
}