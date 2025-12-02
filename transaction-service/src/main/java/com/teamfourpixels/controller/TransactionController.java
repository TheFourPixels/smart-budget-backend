package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;
    private static final Long DUMMY_USER_ID = 1L;

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

        return service.getTransactionsPage(DUMMY_USER_ID, page, size, categoryId, query, start, end);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncTransactions(
            @RequestParam int year,
            @RequestParam int month) {
        int imported = service.importAndClassifyTransactions(DUMMY_USER_ID, year, month);
        return ResponseEntity.ok(Map.of(
                "status", "imported",
                "message", "Импортировано " + imported + " транзакций"
        ));
    }

    @PatchMapping("/{id}/category")
    public TransactionDto updateCategory(@PathVariable Long id,
                                         @RequestBody Map<String, Long> body) {
        Long categoryId = body.get("categoryId");
        if (categoryId == null) throw new IllegalArgumentException("categoryId required");
        return service.updateTransactionCategory(DUMMY_USER_ID, id, categoryId);
    }

    @GetMapping("/{id}")
    public TransactionDto details(@PathVariable Long id) {
        return service.getTransactionDetails(DUMMY_USER_ID, id);
    }

    @PostMapping
    public TransactionDto create(@RequestBody CreateTransactionRequest req) {
        return service.createTransaction(DUMMY_USER_ID, req);
    }
}