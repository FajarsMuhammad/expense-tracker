package com.fajars.expensetracker.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Filter criteria for exporting data.
 * Unlike ReportFilter, this doesn't include pagination fields since exports return all matching records.
 */
@Schema(description = "Filter criteria for data export")
public record ExportFilter(

    @Schema(description = "Start date of the period (inclusive)", example = "2025-11-09")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,

    @Schema(description = "End date of the period (inclusive)", example = "2025-12-08")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate,

    @Schema(description = "List of wallet IDs to include (empty = all wallets)")
    List<UUID> walletIds,

    @Schema(description = "List of category IDs to include (empty = all categories)")
    List<UUID> categoryIds,

    @Schema(description = "Transaction type filter (INCOME, EXPENSE, or null for both)")
    String type
) {

    /**
     * Constructor with defaults for empty lists.
     */
    public ExportFilter {
        // Empty lists instead of null
        if (walletIds == null) {
            walletIds = List.of();
        }
        if (categoryIds == null) {
            categoryIds = List.of();
        }
    }

    /**
     * Check if wallet filter is applied.
     */
    public boolean hasWalletFilter() {
        return walletIds != null && !walletIds.isEmpty();
    }

    /**
     * Check if category filter is applied.
     */
    public boolean hasCategoryFilter() {
        return categoryIds != null && !categoryIds.isEmpty();
    }

    /**
     * Check if type filter is applied.
     */
    public boolean hasTypeFilter() {
        return type != null && !type.isBlank();
    }
}
