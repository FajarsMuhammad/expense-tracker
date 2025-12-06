package com.fajars.expensetracker.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an existing debt.
 */
@Schema(description = "Request to update an existing debt")
public record UpdateDebtRequest(

    @NotNull(message = "Debt type is required")
    @Schema(description = "Type of debt: PAYABLE (you owe) or RECEIVABLE (owed to you)", example = "PAYABLE")
    DebtType type,

    @NotBlank(message = "Counterparty name is required")
    @Size(max = 255, message = "Counterparty name must not exceed 255 characters")
    @Schema(description = "Name of the person or entity involved in the debt", example = "John Doe")
    String counterpartyName,

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    @Schema(description = "Total amount of the debt", example = "1200.00")
    Double totalAmount,

    @NotNull(message = "Due date is required")
    @Schema(description = "Due date for the debt", example = "2025-12-31T23:59:59")
    LocalDateTime dueDate,

    @Size(max = 500, message = "Note must not exceed 500 characters")
    @Schema(description = "Additional notes about the debt", example = "Updated business loan")
    String note
) {
}
