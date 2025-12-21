package com.fajars.expensetracker.report.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Category breakdown for financial reports.
 * Shows total amount and percentage for a specific category.
 */
@Schema(description = "Category breakdown with amount and percentage")
public record CategoryBreakdownResponse(

    @Schema(description = "Category ID")
    UUID categoryId,

    @Schema(description = "Category name", example = "Food & Dining")
    String categoryName,

    @Schema(description = "Category type (INCOME or EXPENSE)")
    String categoryType,

    @Schema(description = "Total amount for this category", example = "1500000.00")
    Double totalAmount,

    @Schema(description = "Number of transactions in this category", example = "12")
    Integer transactionCount,

    @Schema(description = "Percentage of total (income or expense)", example = "25.5")
    Double percentage
) {
}
