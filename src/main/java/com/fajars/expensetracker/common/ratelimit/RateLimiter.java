package com.fajars.expensetracker.common.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter using Caffeine cache.
 *
 * Implementation:
 * - Uses in-memory cache with automatic expiration
 * - Thread-safe using AtomicInteger
 * - Separate limits for different operations
 *
 * Limits:
 * - Export operations: 10 requests per minute per user
 * - Future: Can add more operation types
 */
@Component
@Slf4j
public class RateLimiter {

    private static final int EXPORT_LIMIT_PER_MINUTE = 10;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    private final Cache<String, AtomicInteger> cache;

    public RateLimiter() {
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW_DURATION)
            .maximumSize(10000)
            .build();
    }

    /**
     * Check if export operation is allowed for user.
     *
     * @param userId user ID
     * @return true if allowed, false if rate limit exceeded
     * @deprecated Since Milestone 6. Export is now premium-only, rate limiting no longer needed.
     *             Access control enforced via AOP {@link com.fajars.expensetracker.common.security.RequiresPremium}.
     *             This method is kept for backward compatibility but will be removed in future versions.
     */
    @Deprecated(since = "Milestone 6", forRemoval = true)
    public boolean allowExport(UUID userId) {
        return allow(userId, "export", EXPORT_LIMIT_PER_MINUTE);
    }

    /**
     * Check if operation is allowed and increment counter.
     *
     * @param userId user ID
     * @param operation operation type
     * @param limit max requests in window
     * @return true if allowed, false if exceeded
     */
    private boolean allow(UUID userId, String operation, int limit) {
        String key = userId + ":" + operation;

        AtomicInteger counter = cache.get(key, k -> new AtomicInteger(0));

        if (counter == null) {
            counter = new AtomicInteger(0);
            cache.put(key, counter);
        }

        int currentCount = counter.incrementAndGet();

        if (currentCount > limit) {
            log.warn("Rate limit exceeded for user {} on operation: {} (count: {}, limit: {})",
                userId, operation, currentCount, limit);
            return false;
        }

        log.debug("Rate limit check passed for user {} on operation: {} (count: {}/{})",
            userId, operation, currentCount, limit);
        return true;
    }

    /**
     * Get remaining requests for user's export operations.
     *
     * @param userId user ID
     * @return remaining requests in current window
     * @deprecated Since Milestone 6. Export is now premium-only, rate limiting no longer needed.
     *             This method is kept for backward compatibility but will be removed in future versions.
     */
    @Deprecated(since = "Milestone 6", forRemoval = true)
    public int getRemainingExports(UUID userId) {
        String key = userId + ":export";
        AtomicInteger counter = cache.getIfPresent(key);

        if (counter == null) {
            return EXPORT_LIMIT_PER_MINUTE;
        }

        return Math.max(0, EXPORT_LIMIT_PER_MINUTE - counter.get());
    }

    /**
     * Reset rate limit for user (admin operation).
     *
     * @param userId user ID
     * @param operation operation type
     */
    public void reset(UUID userId, String operation) {
        String key = userId + ":" + operation;
        cache.invalidate(key);
        log.info("Rate limit reset for user {} on operation: {}", userId, operation);
    }
}
