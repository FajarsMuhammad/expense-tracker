package com.fajars.expensetracker.report.usecase;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.report.*;
import com.fajars.expensetracker.report.export.CsvExporter;
import com.fajars.expensetracker.report.export.ExcelExporter;
import com.fajars.expensetracker.report.export.PdfExporter;
import com.fajars.expensetracker.subscription.SubscriptionHelper;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Use case for exporting transactions in various formats.
 *
 * Features:
 * - Supports CSV, Excel, PDF formats
 * - Export quota validation (free vs premium)
 * - Metrics and logging
 * - Base64 encoding for easy frontend consumption
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportTransactionsUseCase implements ExportTransactions {

    private final TransactionRepository transactionRepository;
    private final CsvExporter csvExporter;
    private final ExcelExporter excelExporter;
    private final PdfExporter pdfExporter;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final SubscriptionHelper subscriptionHelper;

    @Override
    @Transactional(readOnly = true)
    public ExportResponse export(UUID userId, ExportRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Exporting transactions for user {} in format {}", userId, request.format());

        // 1. Validate export quota (TODO: implement premium check)
        validateExportQuota(userId, request);

        // 2. Fetch transactions with filter
        List<Transaction> transactions = fetchTransactions(userId, request.filter());

        // 3. Generate file based on format
        byte[] fileContent = generateFile(transactions, request.format());

        // 4. Build response
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

        // 5. Record metrics and log
        long duration = System.currentTimeMillis() - startTime;
        metricsService.incrementCounter("export.transactions.total");
        metricsService.recordTimer("export.transactions.duration", startTime);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", userId);
        attributes.put("format", request.format());
        attributes.put("recordCount", transactions.size());
        attributes.put("duration", duration);
        businessEventLogger.logBusinessEvent("TRANSACTIONS_EXPORTED", userId.toString(), attributes);

        log.info("Exported {} transactions for user {} in {}ms", transactions.size(), userId, duration);

        return response;
    }

    /**
     * Validate export quota based on user subscription tier.
     * Free users: limited to 100 records
     * Premium users: up to 10,000 records
     */
    private void validateExportQuota(UUID userId, ExportRequest request) {
        boolean isPremium = subscriptionHelper.isPremiumUser(userId);
        int exportLimit = subscriptionHelper.getExportLimit(userId);

        log.debug("Export quota for user {}: {} (premium: {})", userId, exportLimit, isPremium);

        // Note: The actual limit is enforced in fetchTransactions
    }

    /**
     * Fetch transactions based on filter criteria.
     */
    private List<Transaction> fetchTransactions(UUID userId, ReportFilter filter) {
        int exportLimit = subscriptionHelper.getExportLimit(userId);

        if (filter == null) {
            // Default filter: last 30 days
            filter = new ReportFilter(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now(),
                null, null, null, 0, exportLimit
            );
        }

        // Apply export limit to page size
        int maxSize = Math.min(filter.size(), exportLimit);

        Pageable pageable = PageRequest.of(
            filter.page(),
            maxSize,
            Sort.by(Sort.Direction.DESC, "date")
        );

        Page<Transaction> page = transactionRepository.findByUserIdWithFilters(
            userId,
            filter.hasWalletFilter() && !filter.walletIds().isEmpty() ? filter.walletIds().getFirst() : null,
            filter.hasCategoryFilter() && !filter.categoryIds().isEmpty() ? filter.categoryIds().getFirst() : null,
            filter.hasTypeFilter() ? com.fajars.expensetracker.transaction.TransactionType.valueOf(filter.type()) : null,
            filter.startDate(),
            filter.endDate(),
            pageable
        );

        return page.getContent();
    }

    /**
     * Generate file content based on format.
     */
    private byte[] generateFile(List<Transaction> transactions, ExportFormat format) {
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
