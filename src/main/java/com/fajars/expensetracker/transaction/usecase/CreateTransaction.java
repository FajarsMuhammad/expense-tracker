package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.CreateTransactionRequest;
import com.fajars.expensetracker.transaction.TransactionDto;

import java.util.UUID;

public interface CreateTransaction {
    TransactionDto create(UUID userId, CreateTransactionRequest request);
}
