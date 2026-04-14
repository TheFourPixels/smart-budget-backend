package com.teamfourpixels.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class TransactionMetricsService {

    public final Counter manualTransactionCounter;
    public final Counter autoImportedTransactionCounter;
    public final Counter autoClassificationSuccessCounter;
    public final Counter autoClassificationFailedCounter;
    public final Timer bankSyncTimer;

    public TransactionMetricsService(MeterRegistry registry) {
        this.manualTransactionCounter = Counter.builder("transactions.created.total")
                .tag("source", "manual")
                .description("Транзакции, созданные вручную")
                .register(registry);

        this.autoImportedTransactionCounter = Counter.builder("transactions.created.total")
                .tag("source", "auto_imported")
                .description("Транзакции, импортированные из банка")
                .register(registry);

        this.autoClassificationSuccessCounter = Counter.builder("transactions.classification.total")
                .tag("status", "success")
                .description("Успешно классифицированные транзакции")
                .register(registry);

        this.autoClassificationFailedCounter = Counter.builder("transactions.classification.total")
                .tag("status", "unassigned")
                .description("Транзакции, попавшие в 'Не распределено'")
                .register(registry);

        this.bankSyncTimer = Timer.builder("transactions.bank.sync.time")
                .description("Время, затраченное на синхронизацию с банком")
                .register(registry);
    }
}