package com.fajars.expensetracker.report.api;

import com.fajars.expensetracker.common.security.RequiresPremium;
import com.fajars.expensetracker.report.usecase.exporttransaction.ExportTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for data export functionality.
 *
 * <p><b>PREMIUM Feature:</b> All export functionality requires PREMIUM or TRIAL subscription.
 * FREE users will receive HTTP 403 Forbidden with upgrade prompt.
 *
 * <p>Access control is enforced via AOP using {@link RequiresPremium} annotation.
 * See {@link com.fajars.expensetracker.common.security.PremiumFeatureAspect} for implementation.
 *
 * <p>Supported Formats (PREMIUM only):
 * <ul>
 *   <li>CSV: Comma-separated values with Indonesian localization</li>
 *   <li>EXCEL: Excel file with styling, summary row, and auto-sized columns</li>
 *   <li>PDF: Professional PDF report with summary statistics</li>
 * </ul>
 *
 * <p>Export Limits:
 * <ul>
 *   <li>PREMIUM/TRIAL users: Up to 10,000 records per export</li>
 *   <li>Max date range: 365 days</li>
 * </ul>
 */
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Export", description = "Data export in multiple formats (PREMIUM only)")
public class ExportController {

    private final ExportTransaction exportTransactions;

    /**
     * Export transactions in the specified format.
     *
     * <p><b>PREMIUM Feature:</b> Requires PREMIUM or TRIAL subscription.
     * Access is enforced via {@link RequiresPremium} annotation using AOP.
     *
     * <p>All formats (CSV, Excel, PDF) require PREMIUM subscription.
     * No rate limiting - premium subscription provides sufficient abuse protection.
     *
     * <p>Supported formats:
     * <ul>
     *   <li>CSV: Simple comma-separated values with Indonesian localization</li>
     *   <li>EXCEL: Excel file with styling, summary row, and auto-sized columns</li>
     *   <li>PDF: Professional PDF report with summary statistics</li>
     * </ul>
     *
     * <p>Export limits:
     * <ul>
     *   <li>Records: Up to 10,000 transactions per export</li>
     *   <li>Date range: Max 365 days</li>
     * </ul>
     *
     * @param request           export request with format and filters
     * @return export response with base64-encoded file content
     */
    @PostMapping("/transactions")
    @RequiresPremium(
        feature = "export_transactions",
        message = "Export functionality is available for PREMIUM users only. " +
            "Upgrade to export your transactions in CSV, Excel, or PDF format with up to 10,000 records."
    )
    @Operation(
        summary = "Export transactions (PREMIUM)",
        description = "Export transactions in CSV, Excel, or PDF format. " +
            "PREMIUM users can export up to 10,000 records with max 365-day date range. " +
            "Returns base64-encoded file content for easy download. " +
            "Supports filtering by date range, wallet, category, and transaction type."
    )
    public ResponseEntity<ExportResponse> exportTransactions(
        @Valid @RequestBody ExportRequest request
    ) {
        log.info("POST /api/v1/export/transactions - format: {}", request.format());

        ExportResponse response = exportTransactions.export(request);

        log.info("Export completed: format={}, fileName={}, size={} bytes",
                 request.format(), response.fileName(), response.fileSize());

        return ResponseEntity.ok(response);
    }
}
