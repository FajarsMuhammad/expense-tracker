package com.fajars.expensetracker.payment.usecase;

import com.fajars.expensetracker.payment.midtrans.MidtransWebhookPayload;

/**
 * Use case interface for processing payment webhooks.
 */
public interface ProcessPaymentWebhook {

    /**
     * Process webhook notification from payment gateway.
     * Updates payment status and activates subscription if successful.
     *
     * @param payload the webhook payload
     */
    void processWebhook(MidtransWebhookPayload payload);
}
