package com.fajars.expensetracker.transaction.api;

import com.fajars.expensetracker.transaction.domain.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Transaction response data")
public record TransactionResponse(
        @Schema(description = "Transaction ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Wallet ID", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID walletId,

        @Schema(description = "Wallet name", example = "Main Wallet")
        String walletName,

        @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174002")
        UUID categoryId,

        @Schema(description = "Category name", example = "Groceries")
        String categoryName,

        @Schema(description = "Transaction type", example = "EXPENSE")
        TransactionType type,

        @Schema(description = "Transaction amount", example = "50.00")
        Double amount,

        @Schema(description = "Transaction note", example = "Weekly grocery shopping")
        String note,

        @Schema(description = "Transaction date")
        LocalDateTime date,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getWallet().getId(),
                transaction.getWallet().getName(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getNote(),
                transaction.getDate(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
