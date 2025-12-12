package com.fajars.expensetracker.subscription.usecase;

import com.fajars.expensetracker.subscription.Subscription;
import java.util.UUID;

/**
 * Use case for retrieving user's subscription.
 */
public interface GetUserSubscription {

    /**
     * Get user's active subscription.
     *
     * @param userId user ID
     * @return active subscription
     * @throws com.fajars.expensetracker.common.exception.BusinessException if no subscription found
     */
    Subscription getSubscription(UUID userId);
}
