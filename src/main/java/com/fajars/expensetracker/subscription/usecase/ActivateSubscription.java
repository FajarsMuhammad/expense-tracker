package com.fajars.expensetracker.subscription.usecase;

import com.fajars.expensetracker.subscription.Subscription;
import java.util.UUID;

/**
 * Use case for activating or extending premium subscription.
 * Called after successful payment.
 */
public interface ActivateSubscription {

    /**
     * Activate or extend premium subscription after successful payment.
     * If user has active premium subscription, extend it. Otherwise, create new one.
     *
     * @param userId user ID
     * @param paymentId payment ID (for reference)
     * @param days number of days to add/activate
     * @return activated/extended subscription
     */
    Subscription activateOrExtend(UUID userId, UUID paymentId, int days);
}
