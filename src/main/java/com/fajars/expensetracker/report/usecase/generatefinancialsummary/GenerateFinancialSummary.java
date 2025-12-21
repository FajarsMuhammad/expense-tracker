package com.fajars.expensetracker.report.usecase.generatefinancialsummary;

import com.fajars.expensetracker.report.api.FinancialSummaryResponse;
import com.fajars.expensetracker.report.api.ReportFilter;

/**
 * Use case interface for generating financial summary reports.
 */
public interface GenerateFinancialSummary {

    /**
     * Generate a financial summary report for the given user and filter criteria.
     *
     * @param filter the filter criteria (date range, wallets, etc.)
     * @return the financial summary response
     */
    FinancialSummaryResponse generate(ReportFilter filter);
}
