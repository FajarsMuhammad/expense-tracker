package com.fajars.expensetracker.common.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter for report generation frequency.
 * FREE tier users are limited to 10 reports per day.
 * PREMIUM users have unlimited reports.
 */
@Component
@Slf4j
public class ReportFrequencyLimiter {

    private static final int FREE_TIER_DAILY_LIMIT = 10;
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    // Cache with date-based keys for automatic daily reset
    private final Cache<String, AtomicInteger> cache;

    public ReportFrequencyLimiter() {
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(2))  // Keep cache for 2 days to handle timezone edge cases
            .maximumSize(10000)  // Support up to 10k concurrent users
            .build();
    }

    /**
     * Check if user can generate another report today.
     *
     * @param userId User ID
     * @return true if report is allowed, false if daily limit exceeded
     */
    public boolean allowReport(UUID userId) {
        String key = generateKey(userId);
        AtomicInteger counter = cache.get(key, k -> new AtomicInteger(0));

        int current = counter.incrementAndGet();

        if (current > FREE_TIER_DAILY_LIMIT) {
            log.warn("User {} exceeded daily report limit: {}/{}", userId, current - 1, FREE_TIER_DAILY_LIMIT);
            counter.decrementAndGet(); // Rollback increment
            return false;
        }

        log.debug("User {} report count: {}/{}", userId, current, FREE_TIER_DAILY_LIMIT);
        return true;
    }

    /**
     * Get remaining report quota for today.
     *
     * @param userId User ID
     * @return Number of reports remaining for today
     */
    public int getRemainingReports(UUID userId) {
        String key = generateKey(userId);
        AtomicInteger counter = cache.getIfPresent(key);

        if (counter == null) {
            return FREE_TIER_DAILY_LIMIT;
        }

        return Math.max(0, FREE_TIER_DAILY_LIMIT - counter.get());
    }

    /**
     * Reset user's daily quota (admin function for testing/support).
     *
     * @param userId User ID
     */
    public void reset(UUID userId) {
        String key = generateKey(userId);
        cache.invalidate(key);
        log.info("Reset daily report quota for user: {}", userId);
    }

    /**
     * Generate cache key based on user ID and current date in Jakarta timezone.
     * Keys automatically become invalid at midnight, providing daily reset.
     *
     * @param userId User ID
     * @return Cache key in format "userId:yyyy-MM-dd"
     */
    private String generateKey(UUID userId) {
        LocalDate today = LocalDate.now(JAKARTA_ZONE);
        return userId + ":" + today;
    }
}
