package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.TransactionResponse;

import java.util.UUID;

/**
 * Use Case: Find a transaction by ID
 */
public interface FindTransactionById {
    TransactionResponse findByIdAndUserId(UUID transactionId, UUID userId);
}
