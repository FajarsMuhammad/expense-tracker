package com.fajars.expensetracker.report;

import com.fajars.expensetracker.transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Filter criteria for generating reports. Supports filtering by date range, wallets, categories,
 * and transaction type.
 */
@Schema(description = "Filter criteria for report generation")
public record ReportFilter(

    @Schema(description = "Start date of the period", example = "2025-11-01T00:00:00")
    LocalDateTime startDate,

    @Schema(description = "End date of the period", example = "2025-11-30T23:59:59")
    LocalDateTime endDate,

    @Schema(description = "List of wallet IDs to include (empty = all wallets)")
    List<UUID> walletIds,

    @Schema(description = "List of category IDs to include (empty = all categories)")
    List<UUID> categoryIds,

    @Schema(description = "Transaction type filter (INCOME, EXPENSE, or null for both)")
    String type,

    @Schema(description = "Page number for pagination (0-based)", example = "0")
    Integer page,

    @Schema(description = "Page size for pagination (max 100)", example = "20")
    Integer size
) {

    /**
     * Constructor with defaults and validation.
     */
    public ReportFilter {
        // Set defaults
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0 || size > 100) {
            size = 20;
        }

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

    public UUID firstWalletId() {
        return hasWalletFilter() && !walletIds().isEmpty()
            ? walletIds().getFirst()
            : null;
    }

    public UUID firstCategoryId() {
        return hasCategoryFilter() && !categoryIds().isEmpty()
            ? categoryIds().getFirst()
            : null;
    }

    public TransactionType transactionType() {
        return hasTypeFilter() ? TransactionType.valueOf(type) : null;
    }
}
