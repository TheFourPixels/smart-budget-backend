package com.teamfourpixels.repository;

import com.teamfourpixels.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTransactionIdOrderByTimestampDesc(Long transactionId);

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
}