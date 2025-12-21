package com.fajars.expensetracker.transaction.usecase.findalltransaction;

import com.fajars.expensetracker.transaction.api.TransactionFilter;
import com.fajars.expensetracker.transaction.api.TransactionPageResponse;

/**
 * Use Case: Find all transactions with filters and pagination
 */
public interface FindAllTransactions {
    TransactionPageResponse findWithFilters(TransactionFilter filter);
}
