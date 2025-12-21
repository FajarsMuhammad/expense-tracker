package com.fajars.expensetracker.auth.dto;

import com.fajars.expensetracker.subscription.domain.SubscriptionStatus;
import com.fajars.expensetracker.subscription.domain.SubscriptionTier;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Subscription information included in authentication response")
public record SubscriptionInfo(
    @Schema(description = "Subscription ID")
    UUID id,

    @Schema(description = "Subscription tier (FREE or PREMIUM)")
    SubscriptionTier tier,

    @Schema(description = "Subscription status (TRIAL, ACTIVE, EXPIRED, CANCELLED)")
    SubscriptionStatus status
) {}
