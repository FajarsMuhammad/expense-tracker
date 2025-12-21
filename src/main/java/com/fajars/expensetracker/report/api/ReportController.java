package com.fajars.expensetracker.report.api;

import com.fajars.expensetracker.auth.AuthenticatedUser;
import com.fajars.expensetracker.common.security.RequiresPremium;
import com.fajars.expensetracker.common.validation.DateRangeValidator;
import com.fajars.expensetracker.report.usecase.generatefinancialsummary.GenerateFinancialSummary;
import com.fajars.expensetracker.report.usecase.getcategorybreakdown.GetCategoryBreakdown;
import com.fajars.expensetracker.report.usecase.getincomeexpensetrend.GetIncomeExpenseTrend;
import com.fajars.expensetracker.report.usecase.getincomeexpensetrend.Granularity;
import com.fajars.expensetracker.transaction.api.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for financial reports and analytics.
 *
 * <p><b>PREMIUM Feature:</b> All report endpoints require PREMIUM or TRIAL subscription.
 * FREE users will receive HTTP 403 Forbidden with upgrade prompt.
 *
 * <p>Access control is enforced via AOP using {@link RequiresPremium} annotation.
 * See {@link com.fajars.expensetracker.common.security.PremiumFeatureAspect} for implementation.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/v1/reports/summary - Financial summary with category breakdown</li>
 *   <li>GET /api/v1/reports/trend - Income/expense trend data for charts</li>
 *   <li>GET /api/v1/reports/category-breakdown - Category analysis</li>
 * </ul>
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Financial reports and analytics (PREMIUM only)")
public class ReportController {

    private final GenerateFinancialSummary generateFinancialSummary;
    private final GetIncomeExpenseTrend getIncomeExpenseTrend;
    private final GetCategoryBreakdown getCategoryBreakdown;
    private final DateRangeValidator dateRangeValidator;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    /**
     * Get financial summary report with category breakdown and wallet balances.
     *
     * <p><b>PREMIUM Feature:</b> Requires PREMIUM or TRIAL subscription.
     * Access is enforced via {@link RequiresPremium} annotation using AOP.
     *
     * @param startDate   optional start date (defaults to 30 days ago)
     * @param endDate     optional end date (defaults to today)
     * @param walletIds   optional wallet filter
     * @param categoryIds optional category filter
     * @return financial summary response
     */
    @GetMapping("/summary")
    @RequiresPremium(
        feature = "financial_summary_report",
        message = "Financial summary reports are available for PREMIUM users only. " +
            "Upgrade to access detailed insights, analytics, and unlimited reporting."
    )
    @Operation(
        summary = "Get financial summary (PREMIUM)",
        description =
            "Get comprehensive financial summary including total income, expenses, category breakdown, and wallet balances. "
                +
                "Defaults to last 30 days if no dates provided. Max date range: 365 days for PREMIUM users."
    )
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false) List<UUID> walletIds,
        @RequestParam(required = false) List<UUID> categoryIds
    ) {
        log.info(
            "GET /api/v1/reports/summary - startDate : {}, endDate : {}, walletIds : {}, categoryIds : {}",
            startDate, endDate, walletIds, categoryIds);

        LocalDate start = startDate != null ? startDate : LocalDate.now(JAKARTA_ZONE).minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now(JAKARTA_ZONE);

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.isBefore(LocalDate.now(JAKARTA_ZONE))
            ? end.atTime(23, 59, 59, 999999999)
            : ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        // Validate date range (PREMIUM: max 365 days)
        dateRangeValidator.validatePremiumTier(startDateTime, endDateTime);

        ReportFilter filter = new ReportFilter(
            startDateTime,
            endDateTime,
            walletIds,
            categoryIds,
            null,
            0,
            100
        );

        FinancialSummaryResponse response = generateFinancialSummary.generate(filter);

        log.info("Financial summary generated: {} transactions, netBalance: {}",
                 response.transactionCount(), response.netBalance());

        return ResponseEntity.ok(response);
    }

    /**
     * Get income/expense trend data for charts.
     *
     * <p><b>PREMIUM Feature:</b> Requires PREMIUM or TRIAL subscription.
     * Access is enforced via {@link RequiresPremium} annotation using AOP.
     *
     * @param startDate   optional start date (defaults to 30 days ago)
     * @param endDate     optional end date (defaults to today)
     * @param granularity data granularity (DAILY, WEEKLY, MONTHLY)
     * @param walletIds   optional wallet filter
     * @return list of trend data points
     */
    @GetMapping("/trend")
    @RequiresPremium(
        feature = "income_expense_trend",
        message = "Trend analytics are available for PREMIUM users only. " +
            "Upgrade to visualize your financial patterns over time."
    )
    @Operation(
        summary = "Get income/expense trend (PREMIUM)",
        description =
            "Get time series data for income and expense trends. Returns daily data points that can be aggregated by granularity (DAILY, WEEKLY, MONTHLY). "
                +
                "Max date range: 365 days for PREMIUM users."
    )
    public ResponseEntity<List<TrendDataResponse>> getTrend(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(defaultValue = "DAILY") Granularity granularity,
        @RequestParam(required = false) List<UUID> walletIds
    ) {
        log.info("GET /api/v1/reports/trend - granularity: {}", granularity);

        LocalDate start = startDate != null ? startDate : LocalDate.now(JAKARTA_ZONE).minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now(JAKARTA_ZONE);

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.isBefore(LocalDate.now(JAKARTA_ZONE))
            ? end.atTime(23, 59, 59, 999999999)
            : ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        dateRangeValidator.validatePremiumTier(startDateTime, endDateTime);

        ReportFilter filter = new ReportFilter(
            startDateTime,
            endDateTime,
            walletIds,
            null,
            null,
            0,
            1000
        );

        List<TrendDataResponse> trendData = getIncomeExpenseTrend.get(filter, granularity);

        log.info("Trend data generated: {} data points", trendData.size());

        return ResponseEntity.ok(trendData);
    }

    /**
     * Get category breakdown for pie charts and category analysis.
     *
     * <p><b>PREMIUM Feature:</b> Requires PREMIUM or TRIAL subscription.
     * Access is enforced via {@link RequiresPremium} annotation using AOP.
     *
     * @param startDate optional start date (defaults to 30 days ago)
     * @param endDate   optional end date (defaults to today)
     * @param type      transaction type (INCOME or EXPENSE, defaults to EXPENSE)
     * @param limit     optional limit for top N categories (0 = all)
     * @param walletIds optional wallet filter
     * @return list of category breakdowns with percentages
     */
    @GetMapping("/category-breakdown")
    @RequiresPremium(
        feature = "category_breakdown_analysis",
        message = "Category analysis is available for PREMIUM users only. " +
            "Upgrade to understand your spending patterns by category."
    )
    @Operation(
        summary = "Get category breakdown (PREMIUM)",
        description = "Get category-wise spending/income analysis with percentages. " +
            "Useful for pie charts and category comparison. " +
            "Returns categories sorted by amount (highest first). " +
            "Defaults to EXPENSE type if not specified. Max date range: 365 days for PREMIUM users."
    )
    public ResponseEntity<List<CategoryBreakdownResponse>> getCategoryBreakdown(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(required = false, defaultValue = "EXPENSE") TransactionType type,
        @RequestParam(required = false, defaultValue = "0") Integer limit,
        @RequestParam(required = false) List<UUID> walletIds
    ) {
        log.info("GET /api/v1/reports/category-breakdown - type: {}", type);

        LocalDate start = startDate != null ? startDate : LocalDate.now(JAKARTA_ZONE).minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now(JAKARTA_ZONE);

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.isBefore(LocalDate.now(JAKARTA_ZONE))
            ? end.atTime(23, 59, 59, 999999999)
            : ZonedDateTime.now(JAKARTA_ZONE).toLocalDateTime();

        dateRangeValidator.validatePremiumTier(startDateTime, endDateTime);

        ReportFilter filter = new ReportFilter(
            startDateTime,
            endDateTime,
            walletIds,
            null,
            null,
            0,
            100
        );

        // Get breakdown (all or top N)
        List<CategoryBreakdownResponse> breakdown;
        if (limit != null && limit > 0) {
            breakdown = getCategoryBreakdown.getTopCategories(filter, type, limit);
            log.info("Top {} categories generated", limit);
        } else {
            breakdown = getCategoryBreakdown.get(filter, type);
            log.info("All categories breakdown generated: {} categories", breakdown.size());
        }
        return ResponseEntity.ok(breakdown);
    }
}
