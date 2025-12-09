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
     * Checks eligibility before creating trial.
     *
     * @param userId user ID
     * @return created trial subscription
     * @throws com.fajars.expensetracker.common.exception.BusinessException if user is not eligible
     */
    Subscription createTrial(UUID userId);

    /**
     * Create trial subscription for new user registration.
     * Skips eligibility check since this is the user's first subscription.
     *
     * <p>Since Milestone 6: All new users get 14-day trial automatically.
     * This is their one-time trial opportunity.
     *
     * @param userId user ID
     * @return created trial subscription
     */
    Subscription createTrialForNewUser(UUID userId);
}
