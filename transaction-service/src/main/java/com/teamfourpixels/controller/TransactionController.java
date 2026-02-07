package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.service.TransactionService;
import com.teamfourpixels.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;

    @GetMapping
    public Page<TransactionDto> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long startDateMillis,
            @RequestParam(required = false) Long endDateMillis) {

        Instant start = startDateMillis != null
                ? Instant.ofEpochMilli(startDateMillis)
                : Instant.EPOCH;

        Instant end = endDateMillis != null
                ? Instant.ofEpochMilli(endDateMillis)
                : Instant.now();

        return service.getTransactionsPage(UserContext.getUserId(), page, size, categoryId, query, start, end);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncTransactions(
            @RequestParam int year,
            @RequestParam int month) {

        service.importAndClassifyTransactions(UserContext.getUserId(), year, month);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "status", "processing",
                        "message", "Импорт транзакций запущен в фоновом режиме."
                ));
    }

    @PatchMapping("/{id}/category")
    public TransactionDto updateCategory(@PathVariable Long id,
                                         @RequestBody Map<String, Long> body) {
        Long categoryId = body.get("categoryId");
        if (categoryId == null) throw new IllegalArgumentException("categoryId required");
        return service.updateTransactionCategory(UserContext.getUserId(), id, categoryId);
    }

    @PostMapping("/{id}/split")
    @Operation(summary = "Разделить транзакцию на части")
    public TransactionDto splitTransaction(@PathVariable Long id,
                                           @Valid @RequestBody SplitTransactionRequest request) {
        return service.splitTransaction(UserContext.getUserId(), id, request);
    }

    @GetMapping("/{id}")
    public TransactionDto details(@PathVariable Long id) {
        return service.getTransactionDetails(UserContext.getUserId(), id);
    }

    @PostMapping
    public TransactionDto create(@RequestBody CreateTransactionRequest req) {
        return service.createTransaction(UserContext.getUserId(), req);
    }

    @GetMapping("/categories/{categoryId}/total")
    public CategoryTotalSpentDto getTotalSpentByCategory(@PathVariable Long categoryId) {
        return service.getTotalSpentByCategory(UserContext.getUserId(), categoryId);
    }
}