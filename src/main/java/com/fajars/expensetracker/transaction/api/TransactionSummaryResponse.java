package com.fajars.expensetracker.transaction.api;


import com.fajars.expensetracker.transaction.domain.Transaction;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionSummaryResponse(
    UUID id,
    TransactionType type,
    Double amount,
    String note,
    LocalDateTime date,
    UUID walletId,
    String walletName,
    UUID categoryId,
    String categoryName
) {

    public static TransactionSummaryResponse from(Transaction t) {
        return new TransactionSummaryResponse(
            t.getId(),
            t.getType(),
            t.getAmount(),
            t.getNote(),
            t.getDate(),
            t.getWallet() != null ? t.getWallet().getId() : null,
            t.getWallet() != null ? t.getWallet().getName() : null,
            t.getCategory() != null ? t.getCategory().getId() : null,
            t.getCategory() != null ? t.getCategory().getName() : null
        );
    }
}
