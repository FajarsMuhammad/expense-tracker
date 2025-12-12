package com.fajars.expensetracker.subscription.usecase;

import com.fajars.expensetracker.subscription.Subscription;
import java.util.UUID;

/**
 * Use case for creating trial subscription.
 * Activates 14-day free trial for eligible users.
 */
public interface CreateTrialSubscription {

    /**
     * Create trial subscription for user.
     *
     * @param userId user ID
     * @return created trial subscription
     * @throws com.fajars.expensetracker.common.exception.BusinessException if user is not eligible
     */
    Subscription createTrial(UUID userId);
}
