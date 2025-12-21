package com.fajars.expensetracker.transaction.projection;

import com.fajars.expensetracker.transaction.api.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Projection interface for category breakdown data.
 * Used for category-wise reporting and analysis.
 */
public interface CategoryBreakdown {
    UUID getCategoryId();
    String getCategoryName();
    TransactionType getType();
    BigDecimal getTotalAmount();
    Long getTransactionCount();
}
