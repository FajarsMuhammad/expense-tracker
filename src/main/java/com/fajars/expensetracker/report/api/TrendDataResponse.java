package com.fajars.expensetracker.report.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Trend data point for time series charts.
 * Used for income/expense trends over time.
 */
@Schema(description = "Time series data point for trend charts")
public record TrendDataResponse(

    @Schema(description = "Date of the data point", example = "2025-12-01")
    LocalDate date,

    @Schema(description = "Total income for this period", example = "500000.00")
    Double income,

    @Schema(description = "Total expense for this period", example = "300000.00")
    Double expense,

    @Schema(description = "Net balance (income - expense)", example = "200000.00")
    Double balance
) {

    public TrendDataResponse(LocalDate date, Double income, Double expense) {
        this(date, income, expense, income - expense);
    }
}
