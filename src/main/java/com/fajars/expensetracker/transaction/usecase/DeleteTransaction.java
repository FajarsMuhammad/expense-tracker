package com.fajars.expensetracker.transaction.usecase;

import java.util.UUID;

public interface DeleteTransaction {
    void delete(UUID userId, UUID transactionId);
}
