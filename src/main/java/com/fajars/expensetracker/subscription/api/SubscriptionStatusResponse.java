package com.fajars.expensetracker.subscription.api;

import com.fajars.expensetracker.subscription.domain.SubscriptionStatus;
import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for subscription status.
 */
@Builder
public record SubscriptionStatusResponse(
    SubscriptionTier tier,
    SubscriptionStatus status,
    boolean isPremium,
    boolean isTrial,
    LocalDateTime startedAt,
    LocalDateTime endedAt
) {}
