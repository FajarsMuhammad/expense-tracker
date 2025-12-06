package com.fajars.expensetracker.report.usecase;

import com.fajars.expensetracker.report.Granularity;
import com.fajars.expensetracker.report.ReportFilter;
import com.fajars.expensetracker.report.TrendDataDto;

import java.util.List;
import java.util.UUID;

/**
 * Use case interface for getting income/expense trend data.
 */
public interface GetIncomeExpenseTrend {

    /**
     * Get income and expense trend data for charts.
     *
     * @param userId the ID of the user
     * @param filter the report filter (date range, wallets, etc.)
     * @param granularity the time granularity (DAILY, WEEKLY, MONTHLY)
     * @return list of trend data points ordered by date
     */
    List<TrendDataDto> get(UUID userId, ReportFilter filter, Granularity granularity);
}
