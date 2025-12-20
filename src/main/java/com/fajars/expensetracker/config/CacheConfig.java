package com.fajars.expensetracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine.
 *
 * Caching Strategy:
 * - Financial summaries: 5-minute TTL, max 1000 entries
 * - Trend data: 5-minute TTL, max 1000 entries
 * - Keyword mappings: 30-minute TTL, max 1000 entries
 * - Invalidation: Automatic on transaction creation/update/delete
 *
 * Cache keys are based on userId + filter parameters to ensure
 * different users and different date ranges get separate cache entries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine cache manager with optimized settings for financial data.
     *
     * Cache Names:
     * - financialSummaries: Financial summary reports
     * - trendData: Income/expense trend data
     * - categoryBreakdown: Category breakdown data
     * - topCategories: Top categories data
     *
     * Performance Characteristics:
     * - TTL: 5 minutes (balance between freshness and performance)
     * - Max Size: 1000 entries per cache
     * - Eviction: LRU (Least Recently Used)
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "financialSummaries",
            "trendData",
            "categoryBreakdown",
            "topCategories"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats()  // Enable statistics for monitoring
        );

        return cacheManager;
    }

    /**
     * Configure Caffeine cache manager for keyword mappings.
     *
     * Cache Names:
     * - keyword-category-mapping: Maps keywords to categories
     * - keyword-wallet-mapping: Maps keywords to wallets
     *
     * Performance Characteristics:
     * - TTL: 30 minutes (keyword mappings change less frequently)
     * - Max Size: 1000 entries per cache
     * - Eviction: LRU (Least Recently Used)
     */
    @Bean
    public CacheManager keywordCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "keyword-category-mapping",
            "keyword-wallet-mapping"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats()  // Enable statistics for monitoring
        );

        return cacheManager;
    }
}
