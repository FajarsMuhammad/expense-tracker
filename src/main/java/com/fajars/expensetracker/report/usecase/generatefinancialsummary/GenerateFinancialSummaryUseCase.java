package com.fajars.expensetracker.report.usecase.generatefinancialsummary;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.report.api.CategoryBreakdownResponse;
import com.fajars.expensetracker.report.api.FinancialSummaryResponse;
import com.fajars.expensetracker.report.api.ReportFilter;
import com.fajars.expensetracker.report.api.WalletBalanceResponse;
import com.fajars.expensetracker.transaction.domain.TransactionRepository;
import com.fajars.expensetracker.transaction.api.TransactionType;
import com.fajars.expensetracker.transaction.projection.CategoryBreakdown;
import com.fajars.expensetracker.transaction.projection.TransactionSummary;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Use case for generating financial summary reports.
 *
 * Performance Optimizations:
 * - Uses optimized aggregation queries (single query for totals)
 * - Caching with 5-minute TTL
 * - Minimal N+1 query issues
 *
 * Clean Architecture:
 * - Business logic in use case layer
 * - No controller/HTTP concerns
 * - Pure domain operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateFinancialSummaryUseCase implements GenerateFinancialSummary {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "financialSummaries", key = "#userId + '-' + #filter.startDate() + '-' + #filter.endDate()")
    public FinancialSummaryResponse generate(ReportFilter filter) {
        long startTime = System.currentTimeMillis();
        UUID userId = currentUserProvider.getUserId();
        log.debug("Generating financial summary for user {} with filter: {}", userId, filter);

        // 1. Get aggregated summary (single optimized query)
        TransactionSummary summary = transactionRepository.getSummaryByDateRange(
            userId,
            filter.startDate(),
            filter.endDate(),
            filter.hasWalletFilter() ? filter.walletIds() : null
        );

        Double totalIncome = summary.getTotalIncome().doubleValue();
        Double totalExpense = summary.getTotalExpense().doubleValue();
        Long transactionCount = summary.getTransactionCount();

        // 2. Get category breakdowns (2 separate queries for income and expense)
        List<CategoryBreakdownResponse> incomeByCategory = getCategoryBreakdown(
            userId, filter, TransactionType.INCOME, totalIncome
        );

        List<CategoryBreakdownResponse> expenseByCategory = getCategoryBreakdown(
            userId, filter, TransactionType.EXPENSE, totalExpense
        );

        // 3. Get wallet balances
        List<WalletBalanceResponse> walletBalances = getWalletBalances(userId, filter);

        // 4. Build response
        FinancialSummaryResponse response = new FinancialSummaryResponse(
            filter.startDate(),
            filter.endDate(),
            totalIncome,
            totalExpense,
            totalIncome - totalExpense,
            transactionCount.intValue(),
            incomeByCategory,
            expenseByCategory,
            walletBalances
        );

        // 5. Record metrics and log
        long duration = System.currentTimeMillis() - startTime;
        metricsService.recordTimer("report.financial.summary.duration", startTime);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", userId);
        attributes.put("startDate", filter.startDate());
        attributes.put("endDate", filter.endDate());
        attributes.put("duration", duration);
        businessEventLogger.logBusinessEvent("FINANCIAL_SUMMARY_GENERATED", userId.toString(), attributes);

        log.info("Generated financial summary for user {} in {}ms", userId, duration);

        return response;
    }

    /**
     * Get category breakdown with percentage calculation.
     *
     * @param userId the user ID
     * @param filter the report filter
     * @param type the transaction type (INCOME or EXPENSE)
     * @param total the total amount for percentage calculation
     * @return list of category breakdowns sorted by amount DESC
     */
    private List<CategoryBreakdownResponse> getCategoryBreakdown(
        UUID userId,
        ReportFilter filter,
        TransactionType type,
        Double total
    ) {
        List<CategoryBreakdown> results = transactionRepository.getCategoryBreakdown(
            userId,
            filter.startDate(),
            filter.endDate(),
            type,
            filter.hasWalletFilter() ? filter.walletIds() : null
        );

        return results.stream()
            .map(row -> new CategoryBreakdownResponse(
                row.getCategoryId(),
                row.getCategoryName(),
                type.name(),
                row.getTotalAmount().doubleValue(),
                row.getTransactionCount().intValue(),
                calculatePercentage(row.getTotalAmount().doubleValue(), total)
            ))
            .toList();
    }

    /**
     * Calculate percentage with safe division by zero handling.
     */
    private Double calculatePercentage(Double amount, Double total) {
        if (total == null || total == 0.0) {
            return 0.0;
        }
        return (amount / total) * 100.0;
    }

    /**
     * Get current wallet balances.
     * Handles both filtered and all wallets scenarios.
     */
    private List<WalletBalanceResponse> getWalletBalances(UUID userId, ReportFilter filter) {
        List<Wallet> wallets;

        if (filter.hasWalletFilter()) {
            wallets = walletRepository.findAllById(filter.walletIds());
        } else {
            wallets = walletRepository.findByUserId(userId);
        }

        return wallets.stream()
            .map(this::toWalletBalanceDto)
            .toList();
    }

    /**
     * Convert Wallet entity to WalletBalanceDto with calculated current balance.
     */
    private WalletBalanceResponse toWalletBalanceDto(Wallet wallet) {
        Double currentBalance = calculateCurrentBalance(wallet);

        return new WalletBalanceResponse(
            wallet.getId(),
            wallet.getName(),
            wallet.getCurrency().name(),
            currentBalance
        );
    }

    /**
     * Calculate current balance for a wallet.
     * Balance = initialBalance + sum(income) - sum(expense)
     */
    private Double calculateCurrentBalance(Wallet wallet) {
        Double initialBalance = wallet.getInitialBalance();

        if (wallet.getTransactions() == null || wallet.getTransactions().isEmpty()) {
            return initialBalance;
        }

        Double transactionBalance = wallet.getTransactions().stream()
            .mapToDouble(t -> t.getType() == TransactionType.INCOME
                ? t.getAmount()
                : -t.getAmount())
            .sum();

        return initialBalance + transactionBalance;
    }
}
