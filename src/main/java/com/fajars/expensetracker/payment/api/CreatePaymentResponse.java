package com.fajars.expensetracker.payment.api;

import com.fajars.expensetracker.payment.domain.PaymentStatus;
import com.fajars.expensetracker.payment.domain.PaymentTransaction;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for payment creation.
 * Contains payment details and Snap redirect information.
 */
@Builder
public record CreatePaymentResponse(
    UUID paymentId,
    String orderId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    String snapToken,
    String snapRedirectUrl
) {

    /**
     * Convert PaymentTransaction entity to response DTO.
     *
     * @param payment the payment entity
     * @return response DTO
     */
    public static CreatePaymentResponse from(PaymentTransaction payment) {
        return CreatePaymentResponse.builder()
            .paymentId(payment.getId())
            .orderId(payment.getOrderId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .status(payment.getStatus())
            .snapToken(payment.getSnapToken())
            .snapRedirectUrl(payment.getSnapRedirectUrl())
            .build();
    }
}
