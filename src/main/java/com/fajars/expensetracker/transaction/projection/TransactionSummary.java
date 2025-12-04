package com.fajars.expensetracker.transaction.projection;

import java.math.BigDecimal;

/**
 * Projection interface for transaction summary data.
 * Used for aggregate reporting queries.
 */
public interface TransactionSummary {
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpense();
    Long getTransactionCount();
}
