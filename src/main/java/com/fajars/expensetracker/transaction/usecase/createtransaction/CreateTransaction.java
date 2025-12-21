package com.fajars.expensetracker.transaction.usecase.createtransaction;

import com.fajars.expensetracker.transaction.api.CreateTransactionRequest;
import com.fajars.expensetracker.transaction.api.TransactionResponse;

/**
 * Use Case: Create a new transaction
 */
public interface CreateTransaction {
    TransactionResponse create(CreateTransactionRequest request);
}
