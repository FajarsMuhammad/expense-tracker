package com.fajars.expensetracker.transaction.projection;

import com.fajars.expensetracker.transaction.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionExportRow(
    UUID id,
    LocalDateTime date,
    Double amount,
    TransactionType type,
    String walletName,
    String categoryName,
    String note
) {}
