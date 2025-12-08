package com.fajars.expensetracker.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helper service for subscription-related utility functions.
 * This is a domain service that provides shared logic for use cases.
 *
 * Unlike SubscriptionService (which was a traditional service with business logic),
 * this helper only contains pure utility functions without orchestration logic.
 */
@Component
@RequiredArgsConstructor
public class SubscriptionHelper {

    private final SubscriptionRepository subscriptionRepository;

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
}
