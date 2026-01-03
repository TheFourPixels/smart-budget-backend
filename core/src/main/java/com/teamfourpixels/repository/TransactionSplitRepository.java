package com.teamfourpixels.repository;

import com.teamfourpixels.entity.TransactionSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionSplitRepository extends JpaRepository<TransactionSplit, Long> {
    List<TransactionSplit> findByTransactionId(Long transactionId);
}