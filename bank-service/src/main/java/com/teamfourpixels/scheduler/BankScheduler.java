package com.teamfourpixels.scheduler;

import com.teamfourpixels.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankScheduler {

    private final WebhookService webhookService;

    @Scheduled(fixedRate = 60000)
    public void simulateRealTimeTransaction() {
        log.info("Simulating new transaction in Bank...");
        webhookService.sendNewTransactionSignal(1L);
    }
}
