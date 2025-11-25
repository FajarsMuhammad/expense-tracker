package com.fajars.expensetracker.transaction;

import java.util.Date;
import java.util.UUID;

public record TransactionDto(
        UUID id,
        UUID walletId,
        String walletName,
        UUID categoryId,
        String categoryName,
        String type,
        Double amount,
        String note,
        Date date,
        Date createdAt,
        Date updatedAt
) {
    public static TransactionDto from(Transaction transaction) {
        return new TransactionDto(
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
