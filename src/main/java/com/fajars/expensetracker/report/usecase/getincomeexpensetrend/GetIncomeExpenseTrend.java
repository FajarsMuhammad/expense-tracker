package com.fajars.expensetracker.report.usecase.getincomeexpensetrend;

import com.fajars.expensetracker.report.api.ReportFilter;
import com.fajars.expensetracker.report.api.TrendDataResponse;
import java.util.List;

/**
 * Use case interface for getting income/expense trend data.
 */
public interface GetIncomeExpenseTrend {

    /**
     * Get income and expense trend data for charts.
     *
     * @param filter the report filter (date range, wallets, etc.)
     * @param granularity the time granularity (DAILY, WEEKLY, MONTHLY)
     * @return list of trend data points ordered by date
     */
    List<TrendDataResponse> get(ReportFilter filter, Granularity granularity);
}
