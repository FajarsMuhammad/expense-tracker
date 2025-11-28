package com.fajars.expensetracker.transaction.usecase;

import com.fajars.expensetracker.transaction.TransactionFilter;
import com.fajars.expensetracker.transaction.TransactionResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Use Case: Find all transactions with filters and pagination
 */
public interface FindAllTransactions {
    Page<TransactionResponse> findByUserIdWithFilters(UUID userId, TransactionFilter filter);
}
