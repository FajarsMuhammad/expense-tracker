package com.fajars.expensetracker.transaction.usecase.updatetransaction;

import com.fajars.expensetracker.transaction.api.TransactionResponse;
import com.fajars.expensetracker.transaction.api.UpdateTransactionRequest;

import java.util.UUID;

/**
 * Use Case: Update an existing transaction
 */
public interface UpdateTransaction {
    TransactionResponse update(UUID transactionId, UpdateTransactionRequest request);
}
