package com.fajars.expensetracker.report;

import com.fajars.expensetracker.common.exception.RateLimitExceededException;
import com.fajars.expensetracker.common.ratelimit.RateLimiter;
import com.fajars.expensetracker.common.security.UserContext;
import com.fajars.expensetracker.report.usecase.ExportTransactions;
import com.fajars.expensetracker.subscription.SubscriptionService;
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
    private final RateLimiter rateLimiter;
    private final SubscriptionService subscriptionService;

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

        // Check rate limit (10 exports per minute)
        if (!rateLimiter.allowExport(userId)) {
            int remaining = rateLimiter.getRemainingExports(userId);
            throw new RateLimitExceededException(
                String.format("Export rate limit exceeded. Maximum 10 exports per minute allowed. " +
                    "Please try again later. Remaining: %d", remaining)
            );
        }

        // Check premium features
        boolean isPremium = subscriptionService.isPremiumUser(userId);
        if (!isPremium && (request.format() == ExportFormat.PDF || request.format() == ExportFormat.EXCEL)) {
            throw new IllegalArgumentException(
                String.format("Format %s is only available for premium users. " +
                    "Please upgrade your subscription or use CSV format.", request.format())
            );
        }

        ExportResponse response = exportTransactions.export(userId, request);

        log.info("Export completed: {}, size: {} bytes, remaining exports: {}",
            response.fileName(), response.fileSize(), rateLimiter.getRemainingExports(userId));

        return ResponseEntity.ok(response);
    }
}
