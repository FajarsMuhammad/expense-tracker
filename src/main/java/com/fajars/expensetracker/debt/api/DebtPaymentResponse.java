package com.fajars.expensetracker.debt.api;

import com.fajars.expensetracker.debt.domain.DebtPayment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for debt payment information.
 */
@Schema(description = "Debt payment information response")
public record DebtPaymentResponse(

    @Schema(description = "Unique identifier of the payment")
    UUID id,

    @Schema(description = "ID of the associated debt")
    UUID debtId,

    @Schema(description = "Payment amount")
    Double amount,

    @Schema(description = "When the payment was made")
    LocalDateTime paidAt,

    @Schema(description = "Additional notes about the payment")
    String note
) {

    /**
     * Convert a DebtPayment entity to DebtPaymentResponse DTO.
     */
    public static DebtPaymentResponse from(DebtPayment payment) {
        return new DebtPaymentResponse(
            payment.getId(),
            payment.getDebt().getId(),
            payment.getAmount(),
            payment.getPaidAt(),
            payment.getNote()
        );
    }
}
