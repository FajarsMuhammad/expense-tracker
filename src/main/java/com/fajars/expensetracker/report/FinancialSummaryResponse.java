package com.fajars.expensetracker.report;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for financial summary report.
 * Contains aggregated income, expense, and category breakdown for a given period.
 */
@Schema(description = "Financial summary report with income, expense, and category breakdown")
public record FinancialSummaryResponse(

    @Schema(description = "Start date of the report period")
    LocalDateTime startDate,

    @Schema(description = "End date of the report period")
    LocalDateTime endDate,

    @Schema(description = "Total income for the period", example = "5000000.00")
    Double totalIncome,

    @Schema(description = "Total expense for the period", example = "3000000.00")
    Double totalExpense,

    @Schema(description = "Net balance (income - expense)", example = "2000000.00")
    Double netBalance,

    @Schema(description = "Total number of transactions in the period", example = "45")
    Integer transactionCount,

    @Schema(description = "Breakdown of income by category")
    List<CategoryBreakdownResponse> incomeByCategory,

    @Schema(description = "Breakdown of expense by category")
    List<CategoryBreakdownResponse> expenseByCategory,

    @Schema(description = "Current wallet balances")
    List<WalletBalanceResponse> walletBalances
) {
}
