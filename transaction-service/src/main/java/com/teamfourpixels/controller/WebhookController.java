package com.teamfourpixels.controller;

import com.teamfourpixels.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final TransactionService transactionService;

    @Value("${app.webhook.api-key:super-secret-bank-key}")
    private String expectedApiKey;

    @PostMapping("/bank-signal")
    public ResponseEntity<String> receiveBankSignal(
            @RequestHeader("X-Bank-Token") String apiKey,
            @RequestBody BankSignalRequest request) {
        
        if (!expectedApiKey.equals(apiKey)) {
            log.warn("Неавторизованная попытка вызова webhook");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API Key");
        }

        log.info("Получен сигнал от банка для пользователя {}: {}", request.getUserId(), request.getMessage());
        
        YearMonth now = YearMonth.now();
        transactionService.importAndClassifyTransactions(request.getUserId(), now.getYear(), now.getMonthValue());

        return ResponseEntity.ok("Import started");
    }

    @lombok.Data
    public static class BankSignalRequest {
        private Long userId;
        private String message;
    }
}
