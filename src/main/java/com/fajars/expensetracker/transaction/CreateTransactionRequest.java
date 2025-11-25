package com.fajars.expensetracker.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Date;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull(message = "Wallet ID is required")
        UUID walletId,

        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotNull(message = "Transaction type is required (INCOME or EXPENSE)")
        String type,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Double amount,

        String note,

        @NotNull(message = "Transaction date is required")
        Date date
) {
}
