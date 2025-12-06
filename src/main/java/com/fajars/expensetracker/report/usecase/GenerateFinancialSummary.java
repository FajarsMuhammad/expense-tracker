package com.fajars.expensetracker.report.usecase;

import com.fajars.expensetracker.report.FinancialSummaryResponse;
import com.fajars.expensetracker.report.ReportFilter;

import java.util.UUID;

/**
 * Use case interface for generating financial summary reports.
 */
public interface GenerateFinancialSummary {

    /**
     * Generate a financial summary report for the given user and filter criteria.
     *
     * @param userId the ID of the user requesting the report
     * @param filter the filter criteria (date range, wallets, etc.)
     * @return the financial summary response
     */
    FinancialSummaryResponse generate(UUID userId, ReportFilter filter);
}
