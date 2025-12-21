package com.fajars.expensetracker.subscription.usecase.cancelsubscription;

/**
 * Use case for cancelling premium subscription.
 * Subscription remains active until end date, then downgrades to FREE.
 */
public interface CancelSubscription {

    /**
     * Cancel user's premium subscription.
     *
     * @throws com.fajars.expensetracker.common.exception.BusinessException if no active subscription or already FREE
     */
    void cancel();
}
