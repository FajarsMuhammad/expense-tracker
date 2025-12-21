package com.fajars.expensetracker.subscription.usecase.createfreesubscription;

import com.fajars.expensetracker.subscription.domain.Subscription;
import java.util.UUID;

/**
 * Use case for creating free subscription.
 * Called during user registration or when premium expires.
 */
public interface CreateFreeSubscription {

    /**
     * Create FREE tier subscription for user.
     *
     * @param userId user ID
     * @return created subscription
     */
    Subscription createFree(UUID userId);
}
