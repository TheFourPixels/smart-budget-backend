package com.teamfourpixels.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    private static final String BUDGET_SERVICE_BASE_URL = "http://budget-service:8081/api/v1";
    private static final String BANK_SERVICE_BASE_URL = "http://bank-service:8085/api/v1";

    @Bean
    @Qualifier("budgetWebClient")
    public WebClient budgetWebClient() {
        return WebClient.builder()
                .baseUrl(BUDGET_SERVICE_BASE_URL)
                .build();
    }

    @Bean
    @Qualifier("bankWebClient")
    public WebClient bankWebClient() {
        return WebClient.builder()
                .baseUrl(BANK_SERVICE_BASE_URL)
                .build();
    }
}