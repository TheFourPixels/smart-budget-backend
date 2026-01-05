package com.teamfourpixels.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${service.budget.url}")
    private String budgetUrl;

    @Value("${service.transaction.url}")
    private String transactionUrl;

    @Value("${service.goal.url}")
    private String goalUrl;

    @Bean(name = "budgetClient")
    public WebClient budgetClient() {
        return WebClient.builder().baseUrl(budgetUrl).build();
    }

    @Bean(name = "transactionClient")
    public WebClient transactionClient() {
        return WebClient.builder().baseUrl(transactionUrl).build();
    }

    @Bean(name = "goalClient")
    public WebClient goalClient() {
        return WebClient.builder().baseUrl(goalUrl).build();
    }
}