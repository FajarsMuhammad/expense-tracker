package com.fajars.expensetracker.transaction.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection interface for trend data.
 * Used for time-series reporting and chart data.
 */
public interface TrendData {
    LocalDate getDate();
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpense();
}
