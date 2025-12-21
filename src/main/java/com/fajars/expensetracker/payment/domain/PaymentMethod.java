package com.fajars.expensetracker.payment.domain;

/**
 * Payment method enumeration.
 * Represents the various payment methods supported by Midtrans.
 */
public enum PaymentMethod {
    /**
     * Credit or debit card payment.
     */
    CREDIT_CARD,

    /**
     * Bank transfer (VA - Virtual Account).
     */
    BANK_TRANSFER,

    /**
     * E-wallet payments (GoPay, OVO, DANA, ShopeePay, etc.).
     */
    EWALLET,

    /**
     * Convenience store payment (Alfamart, Indomaret).
     */
    CONVENIENCE_STORE,

    /**
     * Kredivo installment.
     */
    KREDIVO,

    /**
     * Akulaku installment.
     */
    AKULAKU,

    /**
     * Other payment methods.
     */
    OTHER
}
