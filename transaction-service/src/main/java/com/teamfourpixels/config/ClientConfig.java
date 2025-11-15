package com.teamfourpixels.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    private static final String BUDGET_SERVICE_BASE_URL = "http://budget-service:8081/api/v1";
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(BUDGET_SERVICE_BASE_URL)
                .build();
    }
}