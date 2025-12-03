package com.fajars.expensetracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine.
 *
 * Caching Strategy:
 * - Financial summaries: 5-minute TTL, max 1000 entries
 * - Trend data: 5-minute TTL, max 1000 entries
 * - Invalidation: Automatic on transaction creation/update/delete
 *
 * Cache keys are based on userId + filter parameters to ensure
 * different users and different date ranges get separate cache entries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine cache manager with optimized settings.
     *
     * Cache Names:
     * - financialSummaries: Financial summary reports
     * - trendData: Income/expense trend data
     *
     * Performance Characteristics:
     * - TTL: 5 minutes (balance between freshness and performance)
     * - Max Size: 1000 entries per cache
     * - Eviction: LRU (Least Recently Used)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "financialSummaries",
            "trendData"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats()  // Enable statistics for monitoring
        );

        return cacheManager;
    }
}
