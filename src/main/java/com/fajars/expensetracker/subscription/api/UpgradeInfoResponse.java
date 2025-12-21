package com.fajars.expensetracker.subscription.api;

import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import lombok.Builder;

/**
 * Response DTO for upgrade information.
 */
@Builder
public record UpgradeInfoResponse(
    String message,
    SubscriptionTier currentTier,
    SubscriptionTier targetTier,
    Double price,
    String currency,
    String duration,
    boolean premium,
    String paymentEndpoint
) {}
