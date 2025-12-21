package com.fajars.expensetracker.transaction.usecase.deletetransaction;

import java.util.UUID;

public interface DeleteTransaction {
    void delete(UUID transactionId);
}
