package com.fajars.expensetracker.payment.midtrans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Webhook payload from Midtrans notification.
 * Represents the notification body sent by Midtrans after payment status changes.
 */
public record MidtransWebhookPayload(
    @JsonProperty("transaction_status")
    String transactionStatus,

    @JsonProperty("status_code")
    String statusCode,

    @JsonProperty("signature_key")
    String signatureKey,

    @JsonProperty("order_id")
    String orderId,

    @JsonProperty("transaction_id")
    String transactionId,

    @JsonProperty("gross_amount")
    BigDecimal grossAmount,

    @JsonProperty("payment_type")
    String paymentType,

    @JsonProperty("transaction_time")
    String transactionTime,

    @JsonProperty("fraud_status")
    String fraudStatus,

    @JsonProperty("settlement_time")
    String settlementTime,

    @JsonProperty("currency")
    String currency
) {

    /**
     * Check if payment is successful (settlement or capture).
     *
     * @return true if payment is successful
     */
    public boolean isSuccess() {
        return "settlement".equalsIgnoreCase(transactionStatus)
            || "capture".equalsIgnoreCase(transactionStatus);
    }

    /**
     * Check if payment is pending.
     *
     * @return true if payment is pending
     */
    public boolean isPending() {
        return "pending".equalsIgnoreCase(transactionStatus);
    }

    /**
     * Check if payment is denied or failed.
     *
     * @return true if payment failed
     */
    public boolean isFailed() {
        return "deny".equalsIgnoreCase(transactionStatus)
            || "cancel".equalsIgnoreCase(transactionStatus)
            || "expire".equalsIgnoreCase(transactionStatus);
    }

    /**
     * Check if fraud detection flagged this transaction.
     *
     * @return true if fraud is detected
     */
    public boolean isFraudulent() {
        return "deny".equalsIgnoreCase(fraudStatus)
            || "challenge".equalsIgnoreCase(fraudStatus);
    }
}
