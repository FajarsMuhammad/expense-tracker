package com.fajars.expensetracker.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PaymentTransaction entity.
 * Provides data access methods for payment operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, UUID> {

    /**
     * Find payment by order ID (unique identifier sent to payment gateway).
     *
     * @param orderId the order ID
     * @return Optional containing the payment if found
     */
    Optional<PaymentTransaction> findByOrderId(String orderId);

    /**
     * Find payment by idempotency key (for duplicate prevention).
     *
     * @param idempotencyKey the idempotency key
     * @return Optional containing the payment if found
     */
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all payments for a specific user, ordered by creation date.
     *
     * @param userId the user ID
     * @return List of payments
     */
    @Query("SELECT p FROM PaymentTransaction p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    /**
     * Find recent successful payments for a user (for subscription activation).
     *
     * @param userId the user ID
     * @param status the payment status
     * @param limit  number of records to return
     * @return List of successful payments
     */
    @Query("SELECT p FROM PaymentTransaction p " +
           "WHERE p.user.id = :userId AND p.status = :status " +
           "ORDER BY p.paidAt DESC " +
           "LIMIT :limit")
    List<PaymentTransaction> findRecentSuccessfulPayments(
        @Param("userId") UUID userId,
        @Param("status") PaymentStatus status,
        @Param("limit") int limit
    );

    /**
     * Find pending payments that have expired (for cleanup job).
     *
     * @param cutoffTime the time before which payments are considered expired
     * @return List of expired pending payments
     */
    @Query("SELECT p FROM PaymentTransaction p " +
           "WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<PaymentTransaction> findExpiredPendingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count successful payments for a user (for analytics).
     *
     * @param userId the user ID
     * @return count of successful payments
     */
    @Query("SELECT COUNT(p) FROM PaymentTransaction p " +
           "WHERE p.user.id = :userId AND p.status = 'SUCCESS'")
    long countSuccessfulPaymentsByUserId(@Param("userId") UUID userId);

    /**
     * Check if user has any successful payment.
     * Used to determine if user is eligible for trial.
     *
     * @param userId the user ID
     * @return true if user has at least one successful payment
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PaymentTransaction p " +
           "WHERE p.user.id = :userId AND p.status = 'SUCCESS'")
    boolean hasSuccessfulPayment(@Param("userId") UUID userId);

    /**
     * Find payments by subscription ID.
     *
     * @param subscriptionId the subscription ID
     * @return List of payments
     */
    @Query("SELECT p FROM PaymentTransaction p " +
           "WHERE p.subscription.id = :subscriptionId " +
           "ORDER BY p.createdAt DESC")
    List<PaymentTransaction> findBySubscriptionId(@Param("subscriptionId") UUID subscriptionId);
}
