package com.fajars.expensetracker.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Subscription entity.
 * Provides data access methods for subscription operations.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /**
     * Find active subscription for a user.
     * Active subscription has status ACTIVE or TRIAL and not expired.
     *
     * @param userId the user ID
     * @return Optional containing active subscription if found
     */
    @Query("SELECT s FROM Subscription s " +
           "WHERE s.user.id = :userId " +
           "AND s.status IN ('ACTIVE', 'TRIAL') " +
           "AND (s.endedAt IS NULL OR s.endedAt > CURRENT_TIMESTAMP) " +
           "ORDER BY s.startedAt DESC " +
           "LIMIT 1")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") UUID userId);

    /**
     * Find latest subscription for a user (regardless of status).
     *
     * @param userId the user ID
     * @return Optional containing latest subscription if found
     */
    @Query("SELECT s FROM Subscription s " +
           "WHERE s.user.id = :userId " +
           "ORDER BY s.startedAt DESC " +
           "LIMIT 1")
    Optional<Subscription> findLatestSubscriptionByUserId(@Param("userId") UUID userId);

    /**
     * Find all subscriptions for a user.
     *
     * @param userId the user ID
     * @return List of subscriptions
     */
    @Query("SELECT s FROM Subscription s " +
           "WHERE s.user.id = :userId " +
           "ORDER BY s.startedAt DESC")
    List<Subscription> findAllByUserId(@Param("userId") UUID userId);

    /**
     * Find expired subscriptions that need to be downgraded.
     * Used by background job to process expiring subscriptions.
     *
     * @param cutoffTime the time before which subscriptions are considered expired
     * @return List of expired subscriptions
     */
    @Query("SELECT s FROM Subscription s " +
           "WHERE s.status IN ('ACTIVE', 'TRIAL') " +
           "AND s.endedAt IS NOT NULL " +
           "AND s.endedAt < :cutoffTime")
    List<Subscription> findExpiredSubscriptions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Check if user has ever had a successful premium subscription.
     * Used for trial eligibility check.
     *
     * @param userId the user ID
     * @return true if user has had premium subscription
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Subscription s " +
           "WHERE s.user.id = :userId " +
           "AND s.plan = 'PREMIUM' " +
           "AND s.status IN ('ACTIVE', 'TRIAL', 'EXPIRED')")
    boolean hasHadPremiumSubscription(@Param("userId") UUID userId);
}
