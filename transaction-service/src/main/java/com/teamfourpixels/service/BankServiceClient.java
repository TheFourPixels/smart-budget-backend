package com.teamfourpixels.service;

import com.teamfourpixels.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankServiceClient {

    private final WebClient bankWebClient;

    public List<TransactionDto> fetchTransactions(int year, int month) {
        return bankWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/bank/transactions")
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .build())
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .collectList()
                .block();
    }
}