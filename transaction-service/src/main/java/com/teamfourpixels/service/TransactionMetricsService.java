package com.teamfourpixels.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TransactionMetricsService {

    private final Counter manualTransactionCounter;
    private final Counter autoImportedTransactionCounter;
    private final Counter autoClassificationSuccessCounter;
    private final Counter autoClassificationFailedCounter;
    private final Timer bankSyncTimer;

    public void incrementManualTransactions() {
        manualTransactionCounter.increment();
    }

    public void incrementAutoImportedTransactions() {
        autoImportedTransactionCounter.increment();
    }

    public void incrementClassificationSuccess() {
        autoClassificationSuccessCounter.increment();
    }

    public void incrementClassificationFailed() {
        autoClassificationFailedCounter.increment();
    }

    public void recordBankSyncTime(long amount, TimeUnit unit) {
        bankSyncTimer.record(amount, unit);
    }

    public <T> T recordBankSync(Supplier<T> task) {
        return bankSyncTimer.record(task);
    }
}