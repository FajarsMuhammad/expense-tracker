package com.fajars.expensetracker.report.usecase;

import com.fajars.expensetracker.report.CategoryBreakdownResponse;
import com.fajars.expensetracker.report.ReportFilter;
import com.fajars.expensetracker.transaction.TransactionType;

import java.util.List;
import java.util.UUID;

/**
 * Use case interface for getting category breakdown data.
 *
 * Returns category-wise spending/income analysis with percentages.
 * Useful for pie charts and category comparison.
 */
public interface GetCategoryBreakdown {

    /**
     * Get category breakdown for a specific transaction type.
     *
     * @param userId user ID
     * @param filter report filter with date range
     * @param type transaction type (INCOME or EXPENSE)
     * @return list of category breakdowns sorted by amount (descending)
     */
    List<CategoryBreakdownResponse> get(UUID userId, ReportFilter filter, TransactionType type);

    /**
     * Get top N categories by amount.
     *
     * @param userId user ID
     * @param filter report filter with date range
     * @param type transaction type (INCOME or EXPENSE)
     * @param limit maximum number of categories to return
     * @return list of top categories sorted by amount (descending)
     */
    List<CategoryBreakdownResponse> getTopCategories(UUID userId, ReportFilter filter, TransactionType type, int limit);
}
