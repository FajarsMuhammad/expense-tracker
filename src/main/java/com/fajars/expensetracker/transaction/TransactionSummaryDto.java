package com.fajars.expensetracker.transaction;

import com.fajars.expensetracker.transaction.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionSummaryDto(
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
    public static TransactionSummaryDto from(Transaction t) {
        return new TransactionSummaryDto(
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
