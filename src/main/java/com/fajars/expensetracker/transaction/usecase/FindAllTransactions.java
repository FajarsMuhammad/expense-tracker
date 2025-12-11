package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.TransactionFilter;
import com.fajars.expensetracker.transaction.TransactionPageResponse;
import java.util.UUID;

/**
 * Use Case: Find all transactions with filters and pagination
 */
public interface FindAllTransactions {
    TransactionPageResponse findByUserIdWithFilters(UUID userId, TransactionFilter filter);
}
