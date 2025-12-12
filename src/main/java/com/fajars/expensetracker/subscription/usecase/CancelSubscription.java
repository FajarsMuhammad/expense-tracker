package com.fajars.expensetracker.subscription.usecase;

import java.util.UUID;

/**
 * Use case for cancelling premium subscription.
 * Subscription remains active until end date, then downgrades to FREE.
 */
public interface CancelSubscription {

    /**
     * Cancel user's premium subscription.
     *
     * @param userId user ID
     * @throws com.fajars.expensetracker.common.exception.BusinessException if no active subscription or already FREE
     */
    void cancel(UUID userId);
}
