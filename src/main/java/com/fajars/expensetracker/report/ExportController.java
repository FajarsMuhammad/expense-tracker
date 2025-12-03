package com.fajars.expensetracker.report;

import com.fajars.expensetracker.common.security.UserContext;
import com.fajars.expensetracker.report.usecase.ExportTransactions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for data export functionality.
 *
 * Endpoints:
 * - POST /api/v1/export/transactions - Export transactions in CSV/Excel/PDF
 */
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Export", description = "Data export in multiple formats")
public class ExportController {

    private final ExportTransactions exportTransactions;
    private final UserContext userContext;

    /**
     * Export transactions in the specified format.
     *
     * Supported formats:
     * - CSV: Simple comma-separated values with Indonesian localization
     * - EXCEL: Excel file with styling, summary row, and auto-sized columns
     * - PDF: Professional PDF report with summary statistics
     *
     * Free tier users are limited to 100 records per export.
     * Premium users have unlimited exports.
     *
     * @param request export request with format and filters
     * @return export response with base64-encoded file content
     */
    @PostMapping("/transactions")
    @Operation(
        summary = "Export transactions",
        description = "Export transactions in CSV, Excel, or PDF format. " +
            "Free tier: limited to 100 records. " +
            "Returns base64-encoded file content for easy download. " +
            "Supports filtering by date range, wallet, category, and transaction type."
    )
    public ResponseEntity<ExportResponse> exportTransactions(
        @Valid @RequestBody ExportRequest request
    ) {
        UUID userId = userContext.getCurrentUserId();
        log.info("POST /api/v1/export/transactions - userId: {}, format: {}",
            userId, request.format());

        ExportResponse response = exportTransactions.export(userId, request);

        log.info("Export completed: {}, size: {} bytes",
            response.fileName(), response.fileSize());

        return ResponseEntity.ok(response);
    }
}
