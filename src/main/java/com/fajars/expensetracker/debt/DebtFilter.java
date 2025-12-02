package com.fajars.expensetracker.debt;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filter criteria for querying debts.
 */
@Schema(description = "Filter criteria for debt queries")
public record DebtFilter(

    @Schema(description = "Filter by debt status", example = "OPEN")
    DebtStatus status,

    @Schema(description = "Filter only overdue debts", example = "true")
    Boolean overdue,

    @Schema(description = "Page number (0-based)", example = "0")
    Integer page,

    @Schema(description = "Page size (max 100)", example = "20")
    Integer size
) {

    public DebtFilter {
        // Provide defaults
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size <= 0 || size > 100) ? 20 : size;
    }
}
