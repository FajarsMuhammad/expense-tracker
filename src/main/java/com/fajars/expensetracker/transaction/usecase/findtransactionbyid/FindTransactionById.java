package com.fajars.expensetracker.transaction.usecase.findtransactionbyid;

import com.fajars.expensetracker.transaction.api.TransactionResponse;

import java.util.UUID;

/**
 * Use Case: Find a transaction by ID
 */
public interface FindTransactionById {
    TransactionResponse findByIdAndUserId(UUID transactionId);
}
