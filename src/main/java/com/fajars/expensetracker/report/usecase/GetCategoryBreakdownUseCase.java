package com.fajars.expensetracker.report.usecase;

import com.fajars.expensetracker.report.CategoryBreakdownResponse;
import com.fajars.expensetracker.report.ReportFilter;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.transaction.TransactionType;
import com.fajars.expensetracker.transaction.projection.CategoryBreakdown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Use case for getting category breakdown data.
 * <p>
 * Features: - Returns category-wise analysis with amounts and percentages - Calculates percentage
 * of total for each category - Sorted by amount (highest first) - Cached for performance - Supports
 * top N filtering
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetCategoryBreakdownUseCase implements GetCategoryBreakdown {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryBreakdown", key = "#userId + '-' + #filter.startDate() + '-' + #filter.endDate() + '-' + #type")
    public List<CategoryBreakdownResponse> get(UUID userId, ReportFilter filter, TransactionType type) {
        log.debug("Getting category breakdown for user: {}, type: {}", userId, type);

        // Apply defaults if not provided
        if (filter == null) {
            filter = new ReportFilter(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now(),
                null, null, null, 0, 100
            );
        }

        // Fetch category breakdown from repository
        List<CategoryBreakdown> results = transactionRepository.getCategoryBreakdown(
            userId,
            filter.startDate(),
            filter.endDate(),
            type,
            filter.hasWalletFilter() && !filter.walletIds().isEmpty() ? filter.walletIds() : null
        );

        // Calculate total amount for percentage calculation
        double totalAmount = results.stream()
            .mapToDouble(row -> row.getTotalAmount().doubleValue())
            .sum();

        // Convert to DTOs with percentage
        List<CategoryBreakdownResponse> breakdown = new ArrayList<>();
        for (CategoryBreakdown row : results) {
            UUID categoryId = row.getCategoryId();
            String categoryName = row.getCategoryName();
            String categoryType = row.getType().toString();
            double amount = row.getTotalAmount().doubleValue();
            Integer count = row.getTransactionCount().intValue();

            // Calculate percentage
            Double percentage = totalAmount > 0 ? (amount / totalAmount) * 100 : 0.0;

            breakdown.add(new CategoryBreakdownResponse(
                categoryId,
                categoryName,
                categoryType,
                amount,
                count,
                percentage
            ));
        }

        log.info("Category breakdown generated: {} categories, total amount: {}",
                 breakdown.size(), totalAmount);

        return breakdown;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "topCategories", key = "#userId + '-' + #filter.startDate() + '-' + #filter.endDate() + '-' + #type + '-' + #limit")
    public List<CategoryBreakdownResponse> getTopCategories(
        UUID userId, ReportFilter filter, TransactionType type, int limit) {
        log.debug("Getting top {} categories for user: {}, type: {}", limit, userId, type);

        List<CategoryBreakdownResponse> allCategories = get(userId, filter, type);

        // Return top N categories (already sorted by amount DESC)
        return allCategories.stream()
            .limit(limit)
            .toList();
    }
}
