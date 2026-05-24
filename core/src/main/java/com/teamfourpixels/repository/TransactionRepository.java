package com.teamfourpixels.repository;

import com.teamfourpixels.entity.Transaction;
import com.teamfourpixels.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByUserIdAndBankTransactionRefId(Long userId, String bankTransactionRefId);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.categoryId = :categoryId " +
            "AND t.type = :type")
    BigDecimal getTotalSpentByCategory(
            Long userId,
            Long categoryId,
            OperationType type
    );
    @Modifying
    @Query("UPDATE Transaction t SET t.categoryId = :uncategorizedId WHERE t.categoryId = :deletedCategoryId AND t.userId = :userId")
    void reassignCategoryToUncategorized(Long deletedCategoryId, Long userId, Long uncategorizedId);
}