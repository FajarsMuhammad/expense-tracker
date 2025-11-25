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
}
