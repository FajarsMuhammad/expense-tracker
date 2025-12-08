package com.fajars.expensetracker.payment;

/**
 * Payment provider enumeration.
 * Represents the payment gateway providers integrated with the system.
 */
public enum PaymentProvider {
    /**
     * Midtrans payment gateway (primary provider).
     */
    MIDTRANS,

    /**
     * Manual payment (for admin-initiated subscriptions or special cases).
     */
    MANUAL
}
