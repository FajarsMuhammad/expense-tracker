package com.fajars.expensetracker.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Filter criteria for querying transactions")
public record TransactionFilter(
    @Schema(description = "Filter by wallet ID", example = "123e4567-e89b-12d3-a456-426614174001")
    UUID walletId,

    @Schema(description = "Filter by category ID", example = "123e4567-e89b-12d3-a456-426614174002")
    UUID categoryId,

    @Schema(description = "Filter by transaction type (INCOME or EXPENSE)", example = "EXPENSE")
    TransactionType type,

    @Schema(description = "Filter transactions from this date (inclusive)", example = "2024-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate from,

    @Schema(description = "Filter transactions to this date (inclusive)", example = "2024-12-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate to,

    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    Integer page,

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    Integer size
) {
    public TransactionFilter {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}
