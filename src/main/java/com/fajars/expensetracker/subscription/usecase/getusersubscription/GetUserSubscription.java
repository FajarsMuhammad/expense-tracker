package com.fajars.expensetracker.subscription.usecase.getusersubscription;

import com.fajars.expensetracker.subscription.domain.Subscription;

/**
 * Use case for retrieving user's subscription.
 */
public interface GetUserSubscription {

    /**
     * Get user's active subscription.
     *
     * @return active subscription
     * @throws com.fajars.expensetracker.common.exception.BusinessException if no subscription found
     */
    Subscription getSubscription();
}
