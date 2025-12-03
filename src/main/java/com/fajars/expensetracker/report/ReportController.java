package com.fajars.expensetracker.report;

import com.fajars.expensetracker.common.security.UserContext;
import com.fajars.expensetracker.common.validation.DateRangeValidator;
import com.fajars.expensetracker.report.usecase.GenerateFinancialSummary;
import com.fajars.expensetracker.report.usecase.GetIncomeExpenseTrend;
import com.fajars.expensetracker.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for financial reports and analytics.
 *
 * Endpoints:
 * - GET /api/v1/reports/summary - Financial summary with category breakdown
 * - GET /api/v1/reports/trend - Income/expense trend data for charts
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Financial reports and analytics")
public class ReportController {

    private final GenerateFinancialSummary generateFinancialSummary;
    private final GetIncomeExpenseTrend getIncomeExpenseTrend;
    private final UserContext userContext;
    private final DateRangeValidator dateRangeValidator;
    private final SubscriptionService subscriptionService;

    /**
     * Get financial summary report with category breakdown and wallet balances.
     *
     * @param startDate optional start date (defaults to 30 days ago)
     * @param endDate optional end date (defaults to now)
     * @param walletIds optional wallet filter
     * @param categoryIds optional category filter
     * @return financial summary response
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Get financial summary",
        description = "Get comprehensive financial summary including total income, expenses, category breakdown, and wallet balances. Defaults to last 30 days if no dates provided."
    )
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary(
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate,
        @RequestParam(required = false) List<UUID> walletIds,
        @RequestParam(required = false) List<UUID> categoryIds
    ) {
        UUID userId = userContext.getCurrentUserId();
        log.info("GET /api/v1/reports/summary - userId: {}", userId);

        // Apply defaults if not provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Validate date range based on subscription tier
        boolean isPremium = subscriptionService.isPremiumUser(userId);
        if (isPremium) {
            dateRangeValidator.validatePremiumTier(startDate, endDate);
        } else {
            dateRangeValidator.validateFreeTier(startDate, endDate);
        }

        ReportFilter filter = new ReportFilter(
            startDate,
            endDate,
            walletIds,
            categoryIds,
            null,  // type
            0,     // page
            100    // size (not used for summary)
        );

        FinancialSummaryResponse response = generateFinancialSummary.generate(userId, filter);

        log.info("Financial summary generated: {} transactions, netBalance: {}",
            response.transactionCount(), response.netBalance());

        return ResponseEntity.ok(response);
    }

    /**
     * Get income/expense trend data for charts.
     *
     * @param startDate optional start date (defaults to 30 days ago)
     * @param endDate optional end date (defaults to now)
     * @param granularity data granularity (DAILY, WEEKLY, MONTHLY)
     * @param walletIds optional wallet filter
     * @return list of trend data points
     */
    @GetMapping("/trend")
    @Operation(
        summary = "Get income/expense trend",
        description = "Get time series data for income and expense trends. Returns daily data points that can be aggregated by granularity (DAILY, WEEKLY, MONTHLY)."
    )
    public ResponseEntity<List<TrendDataDto>> getTrend(
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate,
        @RequestParam(defaultValue = "DAILY") Granularity granularity,
        @RequestParam(required = false) List<UUID> walletIds
    ) {
        UUID userId = userContext.getCurrentUserId();
        log.info("GET /api/v1/reports/trend - userId: {}, granularity: {}", userId, granularity);

        // Apply defaults if not provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Validate date range based on subscription tier
        boolean isPremium = subscriptionService.isPremiumUser(userId);
        if (isPremium) {
            dateRangeValidator.validatePremiumTier(startDate, endDate);
        } else {
            dateRangeValidator.validateFreeTier(startDate, endDate);
        }

        ReportFilter filter = new ReportFilter(
            startDate,
            endDate,
            walletIds,
            null,  // categoryIds
            null,  // type
            0,     // page
            1000   // size (max data points)
        );

        List<TrendDataDto> trendData = getIncomeExpenseTrend.get(userId, filter, granularity);

        log.info("Trend data generated: {} data points", trendData.size());

        return ResponseEntity.ok(trendData);
    }
}
