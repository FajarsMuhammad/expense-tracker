package com.fajars.expensetracker.user;

import com.fajars.expensetracker.subscription.SubscriptionStatus;
import com.fajars.expensetracker.subscription.SubscriptionTier;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Complete user profile response including subscription information.
 *
 * <p>This DTO is returned by GET /api/v1/me endpoint and includes:
 * <ul>
 *   <li>User basic info (id, email, name)</li>
 *   <li>User preferences (locale, timezone)</li>
 *   <li>Account metadata (createdAt, updatedAt)</li>
 *   <li>Current subscription details (tier, status, expiry)</li>
 * </ul>
 *
 * @since Milestone 6
 */
@Builder
public record ProfileResponse(
    // User basic info
    UUID id,
    String email,
    String name,

    // User preferences
    String locale,

    // Account metadata
    Date createdAt,
    Date updatedAt,

    // Subscription info
    SubscriptionInfo subscription
) {

    /**
     * Nested subscription information.
     */
    @Builder
    public record SubscriptionInfo(
        UUID subscriptionId,
        SubscriptionTier tier,
        SubscriptionStatus status,
        boolean isPremium,
        boolean isTrial,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer trialDaysRemaining  // null if not trial, 0 if expired
    ) {}
}
