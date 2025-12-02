package com.teamfourpixels.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Value("${service.budget.base-url}")
    private String budgetServiceBaseUrl;

    @Value("${service.bank.base-url}")
    private String bankServiceBaseUrl;

    @Bean
    @Qualifier("budgetWebClient")
    public WebClient budgetWebClient() {
        return WebClient.builder()
                .baseUrl(budgetServiceBaseUrl)
                .build();
    }

    @Bean
    @Qualifier("bankWebClient")
    public WebClient bankWebClient() {
        return WebClient.builder()
                .baseUrl(bankServiceBaseUrl)
                .build();
    }
}