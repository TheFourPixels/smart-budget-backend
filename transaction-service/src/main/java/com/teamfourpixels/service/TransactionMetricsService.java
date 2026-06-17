package com.teamfourpixels.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class TransactionMetricsService {
    private final Counter manualTransactionCounter;
    private final Counter autoImportedCounter;
    private final Counter autoCorrectedCounter;
    private final Counter autoClassificationSuccessCounter;
    private final Counter autoClassificationFailedCounter;
    private final Timer bankSyncTimer;

    public TransactionMetricsService(MeterRegistry registry) {
        this.manualTransactionCounter = Counter.builder("transactions.manual.total")
                .description("Общее количество созданных вручную транзакций")
                .register(registry);

        this.autoImportedCounter = Counter.builder("transactions.auto.imported.total")
                .description("Общее количество автоматически импортированных транзакций")
                .register(registry);

        this.autoCorrectedCounter = Counter.builder("transactions.auto.corrected.total")
                .description("Количество транзакций, где пользователь исправил категорию")
                .register(registry);

        this.autoClassificationSuccessCounter = Counter.builder("transactions.auto.classification.success.total")
                .description("Количество успешных автоклассификаций")
                .register(registry);

        this.autoClassificationFailedCounter = Counter.builder("transactions.auto.classification.failed.total")
                .description("Количество неудачных автоклассификаций")
                .register(registry);

        this.bankSyncTimer = Timer.builder("transactions.bank.sync.time")
                .description("Время синхронизации с банком")
                .register(registry);
    }

    public void incrementManualTransactions() {
        this.manualTransactionCounter.increment();
    }

    public void incrementAutoImported() {
        this.autoImportedCounter.increment();
    }

    public void incrementAutoCorrected() {
        this.autoCorrectedCounter.increment();
    }

    public void incrementClassificationSuccess() {
        this.autoClassificationSuccessCounter.increment();
    }

    public void incrementClassificationFailed() {
        this.autoClassificationFailedCounter.increment();
    }

    public void recordBankSyncTime(long amount, TimeUnit unit) {
        bankSyncTimer.record(amount, unit);
    }

    public <T> T recordBankSync(Supplier<T> task) {
        return bankSyncTimer.record(task);
    }
}
