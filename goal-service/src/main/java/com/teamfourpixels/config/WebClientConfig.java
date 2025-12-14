package com.teamfourpixels.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${service.budget.url}")
    private String budgetUrl;

    @Bean(name = "budgetClient")
    public WebClient budgetClient() {
        return WebClient.builder().baseUrl(budgetUrl).build();
    }

}