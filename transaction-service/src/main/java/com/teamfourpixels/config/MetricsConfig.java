package com.teamfourpixels.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter manualTransactionCounter(MeterRegistry registry) {
        return Counter.builder("transactions.created.total")
                .tag("source", "manual")
                .description("Транзакции, созданные вручную")
                .register(registry);
    }

    @Bean
    public Counter autoImportedTransactionCounter(MeterRegistry registry) {
        return Counter.builder("transactions.created.total")
                .tag("source", "auto_imported")
                .description("Транзакции, импортированные из банка")
                .register(registry);
    }

    @Bean
    public Counter autoClassificationSuccessCounter(MeterRegistry registry) {
        return Counter.builder("transactions.classification.total")
                .tag("status", "success")
                .description("Успешно классифицированные транзакции")
                .register(registry);
    }

    @Bean
    public Counter autoClassificationFailedCounter(MeterRegistry registry) {
        return Counter.builder("transactions.classification.total")
                .tag("status", "unassigned")
                .description("Транзакции, попавшие в 'Не распределено'")
                .register(registry);
    }

    @Bean
    public Timer bankSyncTimer(MeterRegistry registry) {
        return Timer.builder("transactions.bank.sync.time")
                .description("Время, затраченное на синхронизацию с банком")
                .register(registry);
    }
}