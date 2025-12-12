package com.fajars.expensetracker.subscription;

import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.payment.PaymentRepository;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing user subscription tiers and lifecycle.
 * Integrates with payment system to activate/extend subscriptions.
 *
 * @deprecated This service is deprecated in favor of use case pattern.
 *             Use the following use cases instead:
 *             - {@link com.fajars.expensetracker.subscription.usecase.CreateFreeSubscription}
 *             - {@link com.fajars.expensetracker.subscription.usecase.CreateTrialSubscription}
 *             - {@link com.fajars.expensetracker.subscription.usecase.ActivateSubscription}
 *             - {@link com.fajars.expensetracker.subscription.usecase.CancelSubscription}
 *             - {@link com.fajars.expensetracker.subscription.usecase.GetUserSubscription}
 *             - {@link com.fajars.expensetracker.subscription.usecase.CheckTrialEligibility}
 *             <p>
 *             For helper methods (isPremiumUser, getExportLimit, etc.), use:
 *             - {@link com.fajars.expensetracker.subscription.SubscriptionHelper}
 *             <p>
 *             This class will be removed in a future version.
 */
@Deprecated(since = "Milestone 5", forRemoval = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private static final int TRIAL_DAYS = 14;
    private static final int MONTHLY_DAYS = 30;

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    /**
     * Check if user has premium subscription.
     *
     * @param userId user ID to check
     * @return true if premium, false if free tier
     */
    public boolean isPremiumUser(UUID userId) {
        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .map(Subscription::isPremium)
            .orElse(false);
    }

    /**
     * Get user's subscription tier.
     *
     * @param userId user ID
     * @return subscription tier
     */
    public SubscriptionTier getUserTier(UUID userId) {
        return isPremiumUser(userId) ? SubscriptionTier.PREMIUM : SubscriptionTier.FREE;
    }

    /**
     * Check if user can access premium feature.
     *
     * @param userId user ID
     * @param feature feature name (for logging)
     * @return true if allowed, false otherwise
     */
    public boolean canAccessPremiumFeature(UUID userId, String feature) {
        boolean isPremium = isPremiumUser(userId);
        if (!isPremium) {
            log.info("User {} attempted to access premium feature: {} (denied)", userId, feature);
            metricsService.incrementCounter("subscription.premium_feature_denied",
                "feature", feature);
        }
        return isPremium;
    }

    /**
     * Get export limit for user's tier.
     *
     * @param userId user ID
     * @return max records per export
     */
    public int getExportLimit(UUID userId) {
        return isPremiumUser(userId) ? 10000 : 100;
    }

    /**
     * Get date range limit for user's tier (in days).
     *
     * @param userId user ID
     * @return max days for date range
     */
    public int getDateRangeLimit(UUID userId) {
        return isPremiumUser(userId) ? 365 : 90;
    }

    /**
     * Create FREE tier subscription for new user.
     * Called during user registration.
     *
     * @param userId user ID
     * @return created subscription
     */
    @Transactional
    public Subscription createFreeSubscription(UUID userId) {
        log.info("Creating FREE subscription for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        Subscription subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .provider(null)
            .providerSubscriptionId(null)
            .startedAt(LocalDateTime.now())
            .endedAt(null) // No expiry for FREE tier
            .build();

        subscription = subscriptionRepository.save(subscription);

        logBusinessEvent("SUBSCRIPTION_CREATED", userId, subscription);
        metricsService.incrementCounter("subscription.created", "tier", "FREE");

        log.info("FREE subscription created for user: {}", userId);
        return subscription;
    }

    /**
     * Create TRIAL subscription for eligible user.
     * User is eligible if they have never had a premium subscription.
     *
     * @param userId user ID
     * @return created trial subscription
     * @throws BusinessException if user is not eligible for trial
     */
    @Transactional
    public Subscription createTrialSubscription(UUID userId) {
        log.info("Creating TRIAL subscription for user: {}", userId);

        // Check eligibility: user must not have previous premium subscription
        boolean hasHadPremium = subscriptionRepository.hasHadPremiumSubscription(userId);
        if (hasHadPremium) {
            throw BusinessException.forbidden("User is not eligible for trial");
        }

        // Check if user has already used trial (via payment)
        boolean hasSuccessfulPayment = paymentRepository.hasSuccessfulPayment(userId);
        if (hasSuccessfulPayment) {
            throw BusinessException.forbidden("User has already used trial period");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        // Cancel any existing FREE subscription
        subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .ifPresent(sub -> {
                sub.cancel();
                subscriptionRepository.save(sub);
            });

        // Create trial subscription
        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .user(user)
            .plan(SubscriptionTier.PREMIUM)
            .status(SubscriptionStatus.TRIAL)
            .provider(null)
            .providerSubscriptionId(null)
            .startedAt(now)
            .endedAt(now.plusDays(TRIAL_DAYS))
            .build();

        subscription = subscriptionRepository.save(subscription);

        logBusinessEvent("TRIAL_STARTED", userId, subscription);
        metricsService.incrementCounter("subscription.trial_started");

        log.info("TRIAL subscription created for user {} (expires: {})",
            userId, subscription.getEndedAt());
        return subscription;
    }

    /**
     * Activate or extend premium subscription after successful payment.
     * If user has active premium subscription, extend it. Otherwise, create new one.
     *
     * @param userId user ID
     * @param paymentId payment ID (for reference)
     * @param days number of days to add/activate
     * @return activated/extended subscription
     */
    @Transactional
    public Subscription activateOrExtendSubscription(UUID userId, UUID paymentId, int days) {
        log.info("Activating/extending subscription for user {} with {} days", userId, days);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.notFound("User not found"));

        Subscription subscription = subscriptionRepository
            .findActiveSubscriptionByUserId(userId)
            .orElse(null);

        if (subscription != null && subscription.isPremium()) {
            // Extend existing premium subscription
            subscription.extendBy(days);
            subscription = subscriptionRepository.save(subscription);

            logBusinessEvent("SUBSCRIPTION_EXTENDED", userId, subscription);
            metricsService.incrementCounter("subscription.extended");

            log.info("Subscription extended for user {} until {}", userId, subscription.getEndedAt());
        } else {
            // Cancel any existing subscription (FREE or expired)
            if (subscription != null) {
                subscription.cancel();
                subscriptionRepository.save(subscription);
            }

            // Create new premium subscription
            LocalDateTime now = LocalDateTime.now();
            subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(user)
                .plan(SubscriptionTier.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .provider("MIDTRANS")
                .providerSubscriptionId(paymentId.toString())
                .startedAt(now)
                .endedAt(now.plusDays(days))
                .build();

            subscription = subscriptionRepository.save(subscription);

            logBusinessEvent("SUBSCRIPTION_ACTIVATED", userId, subscription);
            metricsService.incrementCounter("subscription.activated");

            log.info("Premium subscription activated for user {} until {}",
                userId, subscription.getEndedAt());
        }

        return subscription;
    }

    /**
     * Cancel premium subscription (user-initiated).
     * Subscription will remain active until end date, then downgrade to FREE.
     *
     * @param userId user ID
     */
    @Transactional
    public void cancelSubscription(UUID userId) {
        log.info("Cancelling subscription for user: {}", userId);

        Subscription subscription = subscriptionRepository
            .findActiveSubscriptionByUserId(userId)
            .orElseThrow(() -> BusinessException.notFound("No active subscription found"));

        if (subscription.getPlan() == SubscriptionTier.FREE) {
            throw BusinessException.badRequest("Cannot cancel FREE tier subscription");
        }

        subscription.cancel();
        subscriptionRepository.save(subscription);

        logBusinessEvent("SUBSCRIPTION_CANCELLED", userId, subscription);
        metricsService.incrementCounter("subscription.cancelled");

        log.info("Subscription cancelled for user: {}", userId);
    }

    /**
     * Process expired subscriptions (background job).
     * Downgrades expired premium subscriptions to FREE tier.
     *
     * @return number of subscriptions downgraded
     */
    @Transactional
    public int processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");

        LocalDateTime now = LocalDateTime.now();
        var expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions(now);

        int count = 0;
        for (Subscription subscription : expiredSubscriptions) {
            try {
                // Mark as expired
                subscription.markAsExpired();
                subscriptionRepository.save(subscription);

                // Create new FREE subscription
                createFreeSubscription(subscription.getUser().getId());

                logBusinessEvent("SUBSCRIPTION_EXPIRED",
                    subscription.getUser().getId(), subscription);
                count++;

            } catch (Exception e) {
                log.error("Failed to process expired subscription: {}",
                    subscription.getId(), e);
            }
        }

        metricsService.incrementCounter("subscription.expired.processed", "count", String.valueOf(count));
        log.info("Processed {} expired subscriptions", count);

        return count;
    }

    private void logBusinessEvent(String eventType, UUID userId, Subscription subscription) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("subscriptionId", subscription.getId());
        attributes.put("plan", subscription.getPlan());
        attributes.put("status", subscription.getStatus());
        attributes.put("startedAt", subscription.getStartedAt());
        attributes.put("endedAt", subscription.getEndedAt());

        User user = userRepository.findById(userId).orElse(null);
        String username = user != null ? user.getEmail() : userId.toString();

        businessEventLogger.logBusinessEvent(eventType, username, attributes);
    }
}
