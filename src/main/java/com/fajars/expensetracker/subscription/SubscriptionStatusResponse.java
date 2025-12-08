package com.fajars.expensetracker.subscription;

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
