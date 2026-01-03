package com.teamfourpixels.controller;

import com.teamfourpixels.generator.BankDataGenerator;
import com.teamfourpixels.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bank")
@RequiredArgsConstructor
public class BankController {

    private final BankDataGenerator bankDataGenerator;

    @GetMapping("/transactions")
    public List<TransactionDto> fetchTransactions(
            @RequestParam int year,
            @RequestParam int month) {
        return bankDataGenerator.fetchTransactions(year, month);
    }
}