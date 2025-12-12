package com.fajars.expensetracker.subscription.scheduler;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.subscription.Subscription;
import com.fajars.expensetracker.subscription.SubscriptionRepository;
import com.fajars.expensetracker.subscription.SubscriptionStatus;
import com.fajars.expensetracker.subscription.usecase.CreateFreeSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduled job to process expired trial subscriptions.
 *
 * <p>Runs daily at midnight (Asia/Jakarta timezone) to find all trial subscriptions
 * that have expired and downgrade them to FREE tier.
 *
 * <p>Since Milestone 6: All new users get 14-day trial at registration.
 * This scheduler ensures users are automatically downgraded to FREE after trial expires.
 *
 * <p><b>Execution Schedule:</b>
 * <ul>
 *   <li>Frequency: Daily</li>
 *   <li>Time: 00:00 Asia/Jakarta (midnight)</li>
 *   <li>Timezone: Asia/Jakarta</li>
 *   <li>Initial delay: 1 minute (to allow app startup)</li>
 * </ul>
 *
 * <p><b>What It Does:</b>
 * <ol>
 *   <li>Find all subscriptions with status=TRIAL and endedAt < now</li>
 *   <li>For each expired trial:
 *     <ul>
 *       <li>Mark trial subscription as EXPIRED</li>
 *       <li>Create new FREE subscription for the user</li>
 *       <li>Log business event: TRIAL_EXPIRED_TO_FREE</li>
 *       <li>Track metrics for monitoring</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p><b>Monitoring:</b>
 * - Metrics: subscription.trial_expired (counter)
 * - Business Events: TRIAL_EXPIRED_TO_FREE
 * - Logs: INFO level for each processed trial
 *
 * @since Milestone 6
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessExpiredTrialsScheduler {

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    private final SubscriptionRepository subscriptionRepository;
    private final CreateFreeSubscription createFreeSubscription;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    /**
     * Process expired trial subscriptions and downgrade to FREE.
     *
     * <p>Runs daily at midnight Jakarta time (cron: 0 0 0 * * *)
     * Initial delay: 60 seconds (1 minute) to allow application startup
     *
     * <p>This method is idempotent - safe to run multiple times.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Jakarta")
    @Transactional
    public void processExpiredTrials() {
        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now(JAKARTA_ZONE);

        log.info("Starting expired trial processing job at {}", now);

        try {
            // Find all expired trials
            List<Subscription> expiredTrials = subscriptionRepository.findExpiredSubscriptions(now);

            if (expiredTrials.isEmpty()) {
                log.info("No expired trials found");
                return;
            }

            log.info("Found {} expired trials to process", expiredTrials.size());

            int successCount = 0;
            int failureCount = 0;

            for (Subscription expiredTrial : expiredTrials) {
                try {
                    processExpiredTrial(expiredTrial);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to process expired trial for user {}: {}",
                            expiredTrial.getUser().getId(), e.getMessage(), e);
                    metricsService.incrementCounter("subscription.trial_expired.failed");
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Expired trial processing completed: {} successful, {} failed, duration={}ms",
                    successCount, failureCount, duration);

            // Track metrics
            metricsService.incrementCounter("subscription.trial_expired.job_completed");
            metricsService.recordTimer("subscription.trial_expired.job_duration", startTime);

        } catch (Exception e) {
            log.error("Fatal error during expired trial processing: {}", e.getMessage(), e);
            metricsService.incrementCounter("subscription.trial_expired.job_failed");
            throw e;
        }
    }

    /**
     * Process a single expired trial subscription.
     *
     * @param expiredTrial the expired trial subscription
     */
    private void processExpiredTrial(Subscription expiredTrial) {
        String userId = expiredTrial.getUser().getId().toString();
        String userEmail = expiredTrial.getUser().getEmail();

        log.info("Processing expired trial for user: {} (email: {})", userId, userEmail);

        // Mark trial as EXPIRED
        expiredTrial.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(expiredTrial);

        // Create FREE subscription
        Subscription freeSubscription = createFreeSubscription.createFree(expiredTrial.getUser().getId());

        // Log business event
        logBusinessEvent(expiredTrial, freeSubscription);

        // Track metrics
        metricsService.incrementCounter("subscription.trial_expired");
        metricsService.incrementCounter("subscription.downgraded_to_free");

        log.info("Successfully downgraded user {} from TRIAL to FREE", userId);
    }

    /**
     * Log business event for trial expiration.
     *
     * @param expiredTrial the expired trial subscription
     * @param freeSubscription the new free subscription
     */
    private void logBusinessEvent(Subscription expiredTrial, Subscription freeSubscription) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("expiredTrialId", expiredTrial.getId());
        attributes.put("freeSubscriptionId", freeSubscription.getId());
        attributes.put("trialStartedAt", expiredTrial.getStartedAt());
        attributes.put("trialEndedAt", expiredTrial.getEndedAt());
        attributes.put("downgradedAt", LocalDateTime.now(JAKARTA_ZONE));

        String username = expiredTrial.getUser().getEmail();
        businessEventLogger.logBusinessEvent("TRIAL_EXPIRED_TO_FREE", username, attributes);
    }
}
