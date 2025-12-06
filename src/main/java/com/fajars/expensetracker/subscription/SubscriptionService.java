package com.fajars.expensetracker.subscription;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing user subscription tiers.
 *
 * NOTE: This is a placeholder implementation.
 * In production, this should integrate with actual payment/subscription system.
 *
 * Current Implementation:
 * - All users default to FREE tier
 * - Premium tier checks return false
 *
 * Future Implementation:
 * - Connect to subscription database table
 * - Integrate with payment gateway (Stripe, Midtrans, etc.)
 * - Track subscription status, expiry dates
 * - Handle trial periods
 */
@Service
@Slf4j
public class SubscriptionService {

    /**
     * Check if user has premium subscription.
     *
     * TODO: Replace with actual database lookup
     * Current: Returns false (all users are free tier)
     *
     * @param userId user ID to check
     * @return true if premium, false if free tier
     */
    public boolean isPremiumUser(UUID userId) {
        // TODO: Implement actual subscription check
        // Example:
        // return subscriptionRepository.findByUserId(userId)
        //     .map(sub -> sub.getTier() == SubscriptionTier.PREMIUM && sub.isActive())
        //     .orElse(false);

        log.debug("Checking subscription for user: {} (default: FREE)", userId);
        return false; // Default: all users are free tier
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
}
