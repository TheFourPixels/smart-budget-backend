package com.teamfourpixels.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxService outboxService;

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        outboxService.processPendingEvents();
    }
}