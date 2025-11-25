package com.fajars.expensetracker.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service to record custom business metrics
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter userRegistrationCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter transactionCreatedCounter;
    private final Counter walletCreatedCounter;
    private final Counter categoryCreatedCounter;

    // Timers
    private final Timer transactionCreationTimer;
    private final Timer reportGenerationTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.userRegistrationCounter = Counter.builder("users.registered.total")
                .description("Total number of user registrations")
                .register(meterRegistry);

        this.loginSuccessCounter = Counter.builder("login.attempts.total")
                .description("Total number of login attempts")
                .tag("result", "success")
                .register(meterRegistry);

        this.loginFailureCounter = Counter.builder("login.attempts.total")
                .description("Total number of login attempts")
                .tag("result", "failure")
                .register(meterRegistry);

        this.transactionCreatedCounter = Counter.builder("transactions.created.total")
                .description("Total number of transactions created")
                .register(meterRegistry);

        this.walletCreatedCounter = Counter.builder("wallets.created.total")
                .description("Total number of wallets created")
                .register(meterRegistry);

        this.categoryCreatedCounter = Counter.builder("categories.created.total")
                .description("Total number of categories created")
                .register(meterRegistry);

        // Initialize timers
        this.transactionCreationTimer = Timer.builder("transaction.creation.duration")
                .description("Time taken to create a transaction")
                .register(meterRegistry);

        this.reportGenerationTimer = Timer.builder("report.generation.duration")
                .description("Time taken to generate reports")
                .register(meterRegistry);

        log.info("MetricsService initialized with custom business metrics");
    }

    // User metrics
    public void recordUserRegistration() {
        userRegistrationCounter.increment();
        log.debug("Metrics: User registration recorded");
    }

    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
        log.debug("Metrics: Successful login recorded");
    }

    public void recordLoginFailure() {
        loginFailureCounter.increment();
        log.debug("Metrics: Failed login recorded");
    }

    // Transaction metrics
    public void recordTransactionCreated() {
        transactionCreatedCounter.increment();
        log.debug("Metrics: Transaction creation recorded");
    }

    public void recordTransactionCreationTime(long startTimeMillis) {
        long duration = System.currentTimeMillis() - startTimeMillis;
        transactionCreationTimer.record(duration, TimeUnit.MILLISECONDS);
        log.debug("Metrics: Transaction creation time recorded: {}ms", duration);
    }

    // Wallet metrics
    public void recordWalletCreated() {
        walletCreatedCounter.increment();
        log.debug("Metrics: Wallet creation recorded");
    }

    // Category metrics
    public void recordCategoryCreated() {
        categoryCreatedCounter.increment();
        log.debug("Metrics: Category creation recorded");
    }

    // Report metrics
    public void recordReportGenerationTime(long startTimeMillis) {
        long duration = System.currentTimeMillis() - startTimeMillis;
        reportGenerationTimer.record(duration, TimeUnit.MILLISECONDS);
        log.debug("Metrics: Report generation time recorded: {}ms", duration);
    }

    // Generic counter increment
    public void incrementCounter(String name, String... tags) {
        Counter.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .increment();
        log.debug("Metrics: Counter '{}' incremented", name);
    }

    // Generic timer recording
    public void recordTimer(String name, long startTimeMillis, String... tags) {
        long duration = System.currentTimeMillis() - startTimeMillis;
        Timer.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
        log.debug("Metrics: Timer '{}' recorded: {}ms", name, duration);
    }

    // Gauge recording for active counts
    public void recordActiveWalletsGauge(int count) {
        meterRegistry.gauge("wallets.active.count", count);
        log.debug("Metrics: Active wallets gauge set to {}", count);
    }

    public void recordActiveSubscriptionsGauge(int count) {
        meterRegistry.gauge("subscriptions.active.count", count);
        log.debug("Metrics: Active subscriptions gauge set to {}", count);
    }
}
