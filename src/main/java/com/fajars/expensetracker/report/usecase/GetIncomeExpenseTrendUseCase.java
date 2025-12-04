package com.fajars.expensetracker.report.usecase;

import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.report.Granularity;
import com.fajars.expensetracker.report.ReportFilter;
import com.fajars.expensetracker.report.TrendDataDto;
import com.fajars.expensetracker.transaction.TransactionRepository;
import com.fajars.expensetracker.transaction.projection.TrendData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for generating income/expense trend data for time series charts.
 *
 * Performance:
 * - Single aggregated query
 * - Cached for 5 minutes
 * - Fills gaps with zero values for better UX
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetIncomeExpenseTrendUseCase implements GetIncomeExpenseTrend {

    private final TransactionRepository transactionRepository;
    private final MetricsService metricsService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "trendData", key = "#userId + '-' + #filter.startDate() + '-' + #filter.endDate() + '-' + #granularity")
    public List<TrendDataDto> get(UUID userId, ReportFilter filter, Granularity granularity) {
        long startTime = System.currentTimeMillis();
        log.debug("Getting trend data for user {} with granularity {}", userId, granularity);

        // Get trend data from repository (grouped by date)
        List<TrendData> results = transactionRepository.getTrendData(
            userId,
            filter.startDate(),
            filter.endDate(),
            filter.hasWalletFilter() ? filter.walletIds() : null
        );

        // Convert to DTO
        Map<LocalDate, TrendDataDto> trendMap = results.stream()
            .collect(Collectors.toMap(
                TrendData::getDate,
                row -> new TrendDataDto(
                    row.getDate(),
                    row.getTotalIncome().doubleValue(),
                    row.getTotalExpense().doubleValue()
                ),
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        // Fill gaps with zero values (important for charts)
        List<TrendDataDto> completeData = fillGaps(
            trendMap,
            filter.startDate().toLocalDate(),
            filter.endDate().toLocalDate(),
            granularity
        );

        // Apply granularity aggregation if needed
        List<TrendDataDto> aggregatedData = applyGranularity(completeData, granularity);

        metricsService.recordTimer("report.trend.generation.duration", startTime);
        log.info("Generated {} trend data points for user {} in {}ms",
            aggregatedData.size(), userId, System.currentTimeMillis() - startTime);

        return aggregatedData;
    }

    /**
     * Fill gaps in trend data with zero values.
     * This ensures the chart displays smoothly even when there are no transactions on certain days.
     * Data is returned in descending order (most recent first).
     */
    private List<TrendDataDto> fillGaps(
        Map<LocalDate, TrendDataDto> dataMap,
        LocalDate startDate,
        LocalDate endDate,
        Granularity granularity
    ) {
        List<TrendDataDto> result = new ArrayList<>();
        LocalDate currentDate = endDate; // Start from end date for descending order

        while (!currentDate.isBefore(startDate)) {
            TrendDataDto data = dataMap.getOrDefault(
                currentDate,
                new TrendDataDto(currentDate, 0.0, 0.0)
            );
            result.add(data);

            // Move to previous date based on granularity for descending order
            currentDate = switch (granularity) {
                case DAILY -> currentDate.minusDays(1);
                case WEEKLY -> currentDate.minusWeeks(1);
                case MONTHLY -> currentDate.minusMonths(1);
            };
        }

        return result;
    }

    /**
     * Apply granularity aggregation.
     * For WEEKLY and MONTHLY, aggregate daily data into larger periods.
     */
    private List<TrendDataDto> applyGranularity(List<TrendDataDto> dailyData, Granularity granularity) {
        if (granularity == Granularity.DAILY) {
            return dailyData;  // No aggregation needed
        }

        Map<LocalDate, List<TrendDataDto>> grouped = dailyData.stream()
            .collect(Collectors.groupingBy(
                dto -> getPeriodStart(dto.date(), granularity),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        return grouped.entrySet().stream()
            .map(entry -> {
                LocalDate periodStart = entry.getKey();
                List<TrendDataDto> periodData = entry.getValue();

                Double totalIncome = periodData.stream()
                    .mapToDouble(TrendDataDto::income)
                    .sum();

                Double totalExpense = periodData.stream()
                    .mapToDouble(TrendDataDto::expense)
                    .sum();

                return new TrendDataDto(periodStart, totalIncome, totalExpense);
            })
            .toList();
    }

    /**
     * Get the start date of the period for a given date and granularity.
     */
    private LocalDate getPeriodStart(LocalDate date, Granularity granularity) {
        return switch (granularity) {
            case DAILY -> date;
            case WEEKLY -> date.with(java.time.DayOfWeek.MONDAY);  // Start of week
            case MONTHLY -> date.withDayOfMonth(1);  // Start of month
        };
    }
}
