package com.fajars.expensetracker.report.usecase;

import com.fajars.expensetracker.report.ExportRequest;
import com.fajars.expensetracker.report.ExportResponse;

import java.util.UUID;

/**
 * Use case interface for exporting transactions.
 */
public interface ExportTransactions {

    /**
     * Export transactions in the specified format.
     *
     * @param userId the ID of the user requesting the export
     * @param request the export request with format and filters
     * @return the export response with file content
     */
    ExportResponse export(UUID userId, ExportRequest request);
}
