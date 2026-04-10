package com.teamfourpixels.controller;

import com.teamfourpixels.entity.AuditLog;
import com.teamfourpixels.repository.AuditLogRepository;
import com.teamfourpixels.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<List<AuditLog>> getTransactionHistory(@PathVariable Long transactionId) {
        List<AuditLog> history = auditLogRepository.findByTransactionIdOrderByTimestampDesc(transactionId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/user")
    public ResponseEntity<List<AuditLog>> getUserHistory() {
        Long userId = UserContext.getUserId();
        List<AuditLog> history = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(history);
    }
}