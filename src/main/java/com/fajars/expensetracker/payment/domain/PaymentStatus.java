package com.fajars.expensetracker.payment.domain;

/**
 * Payment transaction status enumeration.
 * Represents the lifecycle states of a payment transaction.
 */
public enum PaymentStatus {
    /**
     * Payment has been created but not yet paid by user.
     * Waiting for user to complete payment on Midtrans Snap page.
     */
    PENDING,

    /**
     * Payment has been successfully completed and verified.
     * Subscription should be activated/extended.
     */
    SUCCESS,

    /**
     * Payment failed due to insufficient funds, declined card, or other payment issues.
     */
    FAILED,

    /**
     * Payment link has expired (typically after 24 hours).
     * User needs to create a new payment.
     */
    EXPIRED,

    /**
     * Payment was cancelled by user or system.
     */
    CANCELLED
}
