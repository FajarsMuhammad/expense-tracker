package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.TransactionResponse;
import com.fajars.expensetracker.transaction.UpdateTransactionRequest;

import java.util.UUID;

/**
 * Use Case: Update an existing transaction
 */
public interface UpdateTransaction {
    TransactionResponse update(UUID userId, UUID transactionId, UpdateTransactionRequest request);
}
