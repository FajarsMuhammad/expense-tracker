package com.fajars.expensetracker.payment.usecase.createpayment;

import com.fajars.expensetracker.payment.api.CreatePaymentResponse;

/**
 * Use case interface for creating subscription payment.
 */
public interface CreateSubscriptionPayment {

    /**
     * Create a new subscription payment transaction.
     *
     * @param createPaymentCmd   optional idempotency key for duplicate prevention
     * @return payment response with Snap token and redirect URL
     */
    CreatePaymentResponse createPayment(CreatePaymentCmd createPaymentCmd);
}
