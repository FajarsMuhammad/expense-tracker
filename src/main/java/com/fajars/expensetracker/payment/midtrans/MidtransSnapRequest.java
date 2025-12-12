package com.fajars.expensetracker.payment.midtrans;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for Midtrans Snap API.
 * Used to create payment transaction.
 */
@Builder
public record MidtransSnapRequest(
    @JsonProperty("transaction_details")
    TransactionDetails transactionDetails,

    @JsonProperty("customer_details")
    CustomerDetails customerDetails,

    @JsonProperty("item_details")
    List<ItemDetails> itemDetails,

    @JsonProperty("enabled_payments")
    List<String> enabledPayments,

    @JsonProperty("expiry")
    Expiry expiry
) {

    /**
     * Transaction details sub-object.
     */
    @Builder
    public record TransactionDetails(
        @JsonProperty("order_id")
        String orderId,

        @JsonProperty("gross_amount")
        BigDecimal grossAmount
    ) {}

    /**
     * Customer details sub-object.
     */
    @Builder
    public record CustomerDetails(
        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("email")
        String email,

        @JsonProperty("phone")
        String phone
    ) {}

    /**
     * Item details sub-object.
     */
    @Builder
    public record ItemDetails(
        @JsonProperty("id")
        String id,

        @JsonProperty("price")
        BigDecimal price,

        @JsonProperty("quantity")
        int quantity,

        @JsonProperty("name")
        String name
    ) {}

    /**
     * Expiry configuration sub-object.
     */
    @Builder
    public record Expiry(
        @JsonProperty("unit")
        String unit,

        @JsonProperty("duration")
        int duration
    ) {}
}
