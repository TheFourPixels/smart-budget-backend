package com.teamfourpixels.service;

import com.teamfourpixels.dto.TransactionDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class BankServiceClient {

    private final WebClient webClient;

    public BankServiceClient(@Qualifier("bankWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<TransactionDto> fetchTransactions(int year, int month) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bank/transactions")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .collectList()
                .block();
    }
}