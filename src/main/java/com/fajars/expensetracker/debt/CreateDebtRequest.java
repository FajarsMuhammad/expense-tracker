package com.fajars.expensetracker.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new debt.
 */
@Schema(description = "Request to create a new debt")
public record CreateDebtRequest(

    @NotBlank(message = "Counterparty name is required")
    @Size(max = 255, message = "Counterparty name must not exceed 255 characters")
    @Schema(description = "Name of the person or entity involved in the debt", example = "John Doe")
    String counterpartyName,

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    @Schema(description = "Total amount of the debt", example = "1000.00")
    Double totalAmount,

    @Schema(description = "Due date for the debt (optional)", example = "2025-12-31T23:59:59")
    LocalDateTime dueDate,

    @Size(max = 500, message = "Note must not exceed 500 characters")
    @Schema(description = "Additional notes about the debt", example = "Loan for business expenses")
    String note
) {
}
