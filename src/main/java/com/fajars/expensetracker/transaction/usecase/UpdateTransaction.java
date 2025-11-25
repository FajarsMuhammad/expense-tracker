package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.TransactionDto;
import com.fajars.expensetracker.transaction.UpdateTransactionRequest;

import java.util.UUID;

public interface UpdateTransaction {
    TransactionDto update(UUID userId, UUID transactionId, UpdateTransactionRequest request);
}
