package com.fajars.expensetracker.payment.usecase;

import java.util.UUID;

/**
 * Use case interface for creating subscription payment.
 */
public interface CreateSubscriptionPayment {

    /**
     * Create a new subscription payment transaction.
     *
     * @param userId           the user ID
     * @param idempotencyKey   optional idempotency key for duplicate prevention
     * @return payment response with Snap token and redirect URL
     */
    CreatePaymentResponse createPayment(UUID userId, String idempotencyKey);
}
