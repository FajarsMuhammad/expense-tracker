package com.fajars.expensetracker.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Request DTO for adding a payment to a debt.
 */
@Schema(description = "Request to add a payment to a debt")
public record AddDebtPaymentRequest(

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    @Schema(description = "Amount being paid", example = "250.00")
    Double amount,

    @Schema(description = "Date and time when payment was made (defaults to now if not provided)")
    LocalDateTime paidAt,

    @Size(max = 500, message = "Note must not exceed 500 characters")
    @Schema(description = "Additional notes about the payment", example = "First installment")
    String note
) {
}
