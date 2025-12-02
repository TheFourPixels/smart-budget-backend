package com.teamfourpixels.repository;

import com.teamfourpixels.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByUserIdAndBankTransactionRefId(Long userId, String bankTransactionRefId);
}