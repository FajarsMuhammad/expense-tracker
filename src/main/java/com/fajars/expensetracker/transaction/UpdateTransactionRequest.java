package com.fajars.expensetracker.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Schema(description = "Request to update a transaction")
public record UpdateTransactionRequest(
        @NotNull(message = "Wallet ID is required")
        @Schema(description = "Wallet ID", example = "123e4567-e89b-12d3-a456-426614174001", required = true)
        UUID walletId,

        @NotNull(message = "Category ID is required")
        @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174002", required = true)
        UUID categoryId,

        @NotNull(message = "Transaction type is required")
        @Schema(description = "Transaction type (INCOME or EXPENSE)", example = "EXPENSE", required = true)
        TransactionType type,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Schema(description = "Transaction amount", example = "50.00", required = true)
        Double amount,

        @Schema(description = "Transaction note", example = "Weekly grocery shopping")
        String note,

        @NotNull(message = "Transaction date is required")
        @Schema(description = "Transaction date", example = "2024-01-15T10:30:00.000Z", required = true)
        LocalDateTime date
) {
}
