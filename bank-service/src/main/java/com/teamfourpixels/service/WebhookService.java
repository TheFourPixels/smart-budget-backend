package com.teamfourpixels.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.transaction-service.webhook-url}")
    private String webhookUrl;

    @Value("${app.transaction-service.api-key:super-secret-bank-key}")
    private String apiKey;

    public void sendNewTransactionSignal(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Bank-Token", apiKey);

            Map<String, Object> body = Map.of(
                    "userId", userId,
                    "message", "NEW_TRANSACTION_SIMULATED"
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
            log.info("Signal sent to transaction-service for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send webhook: {}", e.getMessage());
        }
    }
}
