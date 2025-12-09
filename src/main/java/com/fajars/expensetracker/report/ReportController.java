package com.fajars.expensetracker.report;

import com.fajars.expensetracker.auth.UserIdentity;
import com.fajars.expensetracker.common.exception.BusinessException;
import com.fajars.expensetracker.common.ratelimit.ReportFrequencyLimiter;
import com.fajars.expensetracker.common.validation.DateRangeValidator;
import com.fajars.expensetracker.report.usecase.GenerateFinancialSummary;
import com.fajars.expensetracker.report.usecase.GetCategoryBreakdown;
import com.fajars.expensetracker.report.usecase.GetIncomeExpenseTrend;
import com.fajars.expensetracker.subscription.SubscriptionHelper;
import com.fajars.expensetracker.transaction.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for financial reports and analytics.
 * <p>
 * Endpoints: - GET /api/v1/reports/summary - Financial summary with category breakdown - GET
 * /api/v1/reports/trend - Income/expense trend data for charts
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Financial reports and analytics")
public class ReportController {

    private final GenerateFinancialSummary generateFinancialSummary;
    private final GetIncomeExpenseTrend getIncomeExpenseTrend;
    private final GetCategoryBreakdown getCategoryBreakdown;
    private final DateRangeValidator dateRangeValidator;
    private final SubscriptionHelper subscriptionHelper;
    private final ReportFrequencyLimiter reportFrequencyLimiter;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    /**
     * Get financial summary report with category breakdown and wallet balances.
     *
     * @param startDate   optional start date (defaults to 30 days ago)
     * @param endDate     optional end date (defaults to today)
     * @param walletIds   optional wallet filter
     * @param categoryIds optional category filter
     * @return financial summary response
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Get financial summary",
        description = "Get comprehensive financial summary including total income, expenses, category breakdown, and wallet balances. Defaults to last 30 days if no dates provided."
    )
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary(
        @AuthenticationPrincipal UserIdentity userIdentity,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) List<UUID> walletIds,
        @RequestParam(required = false) List<UUID> categoryIds
    ) {
        UUID userId = userIdentity.getUserId();
        log.info("GET /api/v1/reports/summary - userId: {}", userId);

        // Check report frequency limit for FREE tier users
        checkReportFrequencyLimit(userId);

        // Apply defaults if not provided (using Jakarta timezone)
        LocalDate start = startDate != null ? startDate : LocalDate.now(JAKARTA_ZONE).minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now(JAKARTA_ZONE);

        // Convert LocalDate to LocalDateTime (start of day to end of day) in Jakarta timezone
        LocalDateTime startDateTime = start.atStartOfDay();
        // If end date is today or in the future, use current time in Jakarta timezone; otherwise use end of day
        LocalDateTime endDateTime = end.isBefore(LocalDate.now(JAKARTA_ZONE))
            ? end.atTime(23, 59, 59, 999999999)
            : ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        // Validate date range based on subscription tier
        boolean isPremium = subscriptionHelper.isPremiumUser(userId);
        if (isPremium) {
            dateRangeValidator.validatePremiumTier(startDateTime, endDateTime);
        } else {
            dateRangeValidator.validateFreeTier(startDateTime, endDateTime);
        }

        ReportFilter filter = new ReportFilter(
            startDateTime,
            endDateTime,
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
     * @param startDate   optional start date (defaults to 30 days ago)
     * @param endDate     optional end date (defaults to today)
     * @param granularity data granularity (DAILY, WEEKLY, MONTHLY)
     * @param walletIds   optional wallet filter
     * @return list of trend data points
     */
    @GetMapping("/trend")
    @Operation(
        summary = "Get income/expense trend",
        description = "Get time series data for income and expense trends. Returns daily data points that can be aggregated by granularity (DAILY, WEEKLY, MONTHLY)."
    )
    public ResponseEntity<List<TrendDataDto>> getTrend(
        @AuthenticationPrincipal UserIdentity userIdentity,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(defaultValue = "DAILY") Granularity granularity,
        @RequestParam(required = false) List<UUID> walletIds
    ) {
        UUID userId = userIdentity.getUserId();
        log.info("GET /api/v1/reports/trend - userId: {}, granularity: {}", userId, granularity);

        // Check report frequency limit for FREE tier users
        checkReportFrequencyLimit(userId);

        // Apply defaults if not provided (using Jakarta timezone)
        LocalDate start = startDate != null ? startDate : LocalDate.now(JAKARTA_ZONE).minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now(JAKARTA_ZONE);

        // Convert LocalDate to LocalDateTime (start of day to end of day) in Jakarta timezone
        LocalDateTime startDateTime = start.atStartOfDay();
        // If end date is today or in the future, use current time in Jakarta timezone; otherwise use end of day
        LocalDateTime endDateTime = end.isBefore(LocalDate.now(JAKARTA_ZONE))
            ? end.atTime(23, 59, 59, 999999999)
            : ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        // Validate date range based on subscription tier
        boolean isPremium = subscriptionHelper.isPremiumUser(userId);
        if (isPremium) {
            dateRangeValidator.validatePremiumTier(startDateTime, endDateTime);
        } else {
            dateRangeValidator.validateFreeTier(startDateTime, endDateTime);
        }

        ReportFilter filter = new ReportFilter(
            startDateTime,
            endDateTime,
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

    /**
     * Get category breakdown for pie charts and category analysis.
     *
     * @param startDate optional start date (defaults to 30 days ago)
     * @param endDate   optional end date (defaults to today)
     * @param type      transaction type (INCOME or EXPENSE, defaults to EXPENSE)
     * @param limit     optional limit for top N categories (0 = all)
     * @param walletIds optional wallet filter
     * @return list of category breakdowns with percentages
     */
    @GetMapping("/category-breakdown")
    @Operation(
        summary = "Get category breakdown",
        description = "Get category-wise spending/income analysis with percentages. " +
            "Useful for pie charts and category comparison. " +
            "Returns categories sorted by amount (highest first). " +
            "Defaults to EXPENSE type if not specified."
    )
    public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(
        @AuthenticationPrincipal UserIdentity userIdentity,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false, defaultValue = "EXPENSE") TransactionType type,
        @RequestParam(required = false, defaultValue = "0") Integer limit,
        @RequestParam(required = false) List<UUID> walletIds
    ) {
        UUID userId = userIdentity.getUserId();
        log.info("GET /api/v1/reports/category-breakdown - userId: {}, type: {}", userId, type);

        // Check report frequency limit for FREE tier users
        checkReportFrequencyLimit(userId);

        // Apply defaults if not provided (using Jakarta timezone)
        LocalDate start = startDate != null ? startDate : LocalDate.now(JAKARTA_ZONE).minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now(JAKARTA_ZONE);

        // Convert LocalDate to LocalDateTime (start of day to end of day) in Jakarta timezone
        LocalDateTime startDateTime = start.atStartOfDay();
        // If end date is today or in the future, use current time in Jakarta timezone; otherwise use end of day
        LocalDateTime endDateTime = end.isBefore(LocalDate.now(JAKARTA_ZONE))
            ? end.atTime(23, 59, 59, 999999999)
            : ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        // Validate date range based on subscription tier
        boolean isPremium = subscriptionHelper.isPremiumUser(userId);
        if (isPremium) {
            dateRangeValidator.validatePremiumTier(startDateTime, endDateTime);
        } else {
            dateRangeValidator.validateFreeTier(startDateTime, endDateTime);
        }

        ReportFilter filter = new ReportFilter(
            startDateTime,
            endDateTime,
            walletIds,
            null,  // categoryIds
            null,  // type (handled separately)
            0,     // page
            100    // size
        );

        // Get breakdown (all or top N)
        List<CategoryBreakdownDto> breakdown;
        if (limit != null && limit > 0) {
            breakdown = getCategoryBreakdown.getTopCategories(userId, filter, type, limit);
            log.info("Top {} categories generated", limit);
        } else {
            breakdown = getCategoryBreakdown.get(userId, filter, type);
            log.info("All categories breakdown generated: {} categories", breakdown.size());
        }
        return ResponseEntity.ok(breakdown);

    }

    /**
     * Check report frequency limit for FREE tier users.
     * PREMIUM users bypass this check.
     *
     * @param userId User ID
     * @throws BusinessException if FREE user exceeded daily report limit
     */
    private void checkReportFrequencyLimit(UUID userId) {
        // PREMIUM users have unlimited reports
        if (subscriptionHelper.isPremiumUser(userId)) {
            return;
        }

        // Check if FREE user can generate another report today
        if (!reportFrequencyLimiter.allowReport(userId)) {
            int remaining = reportFrequencyLimiter.getRemainingReports(userId);
            throw BusinessException.tooManyRequests(
                    "You have exceeded the daily limit of 10 reports for FREE tier. " +
                    "Remaining: " + remaining + ". " +
                    "Upgrade to PREMIUM for unlimited reports or try again tomorrow."
            );
        }
    }
}
