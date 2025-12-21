package com.fajars.expensetracker.report.usecase.exporttransaction;

import com.fajars.expensetracker.report.api.ExportRequest;
import com.fajars.expensetracker.report.api.ExportResponse;

/**
 * Use case interface for exporting transactions.
 */
public interface ExportTransaction {

    /**
     * Export transactions in the specified format.
     *
     * @param request the export request with format and filters
     * @return the export response with file content
     */
    ExportResponse export(ExportRequest request);
}
