package com.fajars.expensetracker.report.usecase.exporttransaction;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.report.api.ExportRequest;
import com.fajars.expensetracker.report.api.ExportResponse;
import com.fajars.expensetracker.report.api.ReportFilter;
import com.fajars.expensetracker.report.export.CsvExporter;
import com.fajars.expensetracker.report.export.ExcelExporter;
import com.fajars.expensetracker.report.export.PdfExporter;
import com.fajars.expensetracker.subscription.SubscriptionHelper;
import com.fajars.expensetracker.transaction.domain.TransactionExportRepository;
import com.fajars.expensetracker.transaction.projection.TransactionExportRow;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for exporting transactions in various formats.
 *
 * <p><b>PREMIUM Feature:</b> Export is now premium-only.
 * Access control is enforced at controller level via AOP
 * {@link com.fajars.expensetracker.common.security.RequiresPremium}. All users reaching this use
 * case are guaranteed to be PREMIUM or TRIAL tier.
 *
 * <p>Features:
 * <ul>
 *   <li>Supports CSV, Excel, PDF formats</li>
 *   <li>Export limit: 10,000 records for PREMIUM users</li>
 *   <li>Comprehensive metrics and logging</li>
 *   <li>Base64 encoding for easy frontend consumption</li>
 * </ul>
 *
 * <p>Performance:
 * <ul>
 *   <li>Read-only transaction for optimal database performance</li>
 *   <li>Paginated fetch to handle large datasets efficiently</li>
 *   <li>Metrics tracking for monitoring export performance</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportTransactionUseCase implements ExportTransaction {

    private final TransactionExportRepository exportRepository;
    private final CsvExporter csvExporter;
    private final ExcelExporter excelExporter;
    private final PdfExporter pdfExporter;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final SubscriptionHelper subscriptionHelper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public ExportResponse export(ExportRequest request) {
        long startTime = System.currentTimeMillis();
        UUID userId = currentUserProvider.getUserId();

        log.info("Exporting transactions for user {} in format {}", userId, request.format());

        validateExportQuota(userId, request);

        ReportFilter filter = convertToReportFilter(userId, request.filter());

        List<TransactionExportRow> transactions = fetchTransactions(userId, filter);

        byte[] fileContent = generateFile(transactions, request.format());
        String fileName = generateFileName(request.format());
        String contentType = getContentType(request.format());
        String base64Content = Base64.getEncoder().encodeToString(fileContent);

        ExportResponse response = new ExportResponse(
            fileName,
            (long) fileContent.length,
            null,  // downloadUrl - can be implemented with cloud storage
            base64Content,
            contentType,
            LocalDateTime.now().plusHours(1)  // expires in 1 hour
        );

        long duration = System.currentTimeMillis() - startTime;
        metricsService.incrementCounter("export.transactions.total");
        metricsService.recordTimer("export.transactions.duration", startTime);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", userId);
        attributes.put("format", request.format());
        attributes.put("recordCount", transactions.size());
        attributes.put("duration", duration);
        businessEventLogger.logBusinessEvent("TRANSACTIONS_EXPORTED", userId.toString(),
                                             attributes);

        log.info("Exported {} transactions for user {} in {}ms", transactions.size(), userId,
                 duration);

        return response;
    }

    /**
     * Convert ExportFilter to ReportFilter with export-specific settings.
     */
    private ReportFilter convertToReportFilter(UUID userId, ExportFilter exportFilter) {
        int exportLimit = subscriptionHelper.getExportLimit(userId);

        // Handle null filter - default to last 30 days
        if (exportFilter == null) {
            return new ReportFilter(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now(),
                null, null, null, 0, exportLimit
            );
        }

        // Convert LocalDate to LocalDateTime
        LocalDateTime startDateTime = exportFilter.startDate() != null
            ? exportFilter.startDate().atStartOfDay()
            : LocalDateTime.now().minusDays(30);

        LocalDateTime endDateTime = exportFilter.endDate() != null
            ? exportFilter.endDate().atTime(23, 59, 59)
            : LocalDateTime.now();

        return new ReportFilter(
            startDateTime,
            endDateTime,
            exportFilter.walletIds(),
            exportFilter.categoryIds(),
            exportFilter.type(),
            0,  // page - always start from first page for export
            exportLimit  // size - use export limit
        );
    }

    /**
     * Validate export quota for premium users.
     *
     * <p>Note: Since export is premium-only (enforced by AOP), this always returns 10,000 records
     * limit. This method exists for potential future tier differentiation (e.g., PREMIUM vs
     * ENTERPRISE tiers).
     */
    private void validateExportQuota(UUID userId, ExportRequest request) {
        int exportLimit = subscriptionHelper.getExportLimit(userId);

        log.debug("Export quota for user {}: {} records", userId, exportLimit);

        // Note: The actual limit is enforced in fetchTransactions
    }

    /**
     * Fetch transactions based on filter criteria.
     *
     * <p>For exports, we fetch ALL matching transactions up to the export limit,
     * not just a single page. This ensures complete data export.
     */
    private List<TransactionExportRow> fetchTransactions(UUID userId, ReportFilter filter) {
        int exportLimit = subscriptionHelper.getExportLimit(userId);
        int pageSize = Math.min(filter.size(), exportLimit);

        int page = 0;
        int totalFetched = 0;

        List<TransactionExportRow> all = new ArrayList<>();

        List<TransactionExportRow> batch;

        do {
            Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(Sort.Direction.DESC, "date")
            );

            batch = exportRepository.exportTransactions(
                userId,
                filter.firstWalletId(),
                filter.firstCategoryId(),
                filter.transactionType(),
                filter.startDate(),
                filter.endDate(),
                pageable
            );

            if (!batch.isEmpty()) {
                all.addAll(batch);
                totalFetched += batch.size();
            }

            page++;

        } while (!batch.isEmpty() && totalFetched < exportLimit);

        return all;

    }

    /**
     * Generate file content based on format.
     */
    private byte[] generateFile(List<TransactionExportRow> transactions, ExportFormat format) {
        return switch (format) {
            case CSV -> csvExporter.exportTransactionsToCsv(transactions);
            case EXCEL -> excelExporter.exportTransactionsToExcel(transactions);
            case PDF -> pdfExporter.exportTransactionsToPdf(transactions);
        };
    }

    /**
     * Generate filename with timestamp.
     */
    private String generateFileName(ExportFormat format) {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        String extension = switch (format) {
            case CSV -> "csv";
            case EXCEL -> "xlsx";
            case PDF -> "pdf";
        };

        return String.format("transaksi_%s.%s", timestamp, extension);
    }

    /**
     * Get content type for HTTP response.
     */
    private String getContentType(ExportFormat format) {
        return switch (format) {
            case CSV -> "text/csv";
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case PDF -> "application/pdf";
        };
    }
}
