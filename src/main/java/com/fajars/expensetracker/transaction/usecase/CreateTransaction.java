package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.CreateTransactionRequest;
import com.fajars.expensetracker.transaction.TransactionResponse;

import java.util.UUID;

/**
 * Use Case: Create a new transaction
 */
public interface CreateTransaction {
    TransactionResponse create(UUID userId, CreateTransactionRequest request);
}
