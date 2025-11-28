package com.fajars.expensetracker.transaction;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find transaction by ID and user ID for security check
     */
    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.wallet " +
           "LEFT JOIN FETCH t.category " +
           "WHERE t.id = :id AND t.user.id = :userId")
    Optional<Transaction> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);


    @Query(
        value = """
        SELECT DISTINCT t FROM Transaction t
        LEFT JOIN FETCH t.wallet w
        LEFT JOIN FETCH t.category c
        WHERE t.user.id = :userId
          AND (w.id = COALESCE(:walletId, w.id))
          AND (c.id = COALESCE(:categoryId, c.id))
          AND (t.type = COALESCE(:type, t.type))
          AND (t.date >= COALESCE(:fromDate, t.date))
          AND (t.date <= COALESCE(:toDate, t.date))
        """,
        countQuery = """
        SELECT COUNT(DISTINCT t) FROM Transaction t
        WHERE t.user.id = :userId
          AND (t.wallet.id = COALESCE(:walletId, t.wallet.id))
          AND (t.category.id = COALESCE(:categoryId, t.category.id))
          AND (t.type = COALESCE(:type, t.type))
          AND (t.date >= COALESCE(:fromDate, t.date))
          AND (t.date <= COALESCE(:toDate, t.date))
        """
    )
    Page<Transaction> findByUserIdWithFilters(
        @Param("userId") UUID userId,
        @Param("walletId") UUID walletId,
        @Param("categoryId") UUID categoryId,
        @Param("type") TransactionType type,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );



    /**
     * Legacy method - kept for backward compatibility
     */
    List<Transaction> findByUserIdAndWalletIdOrderByDateDesc(UUID userId, UUID walletId);

    /**
     * Legacy method - kept for backward compatibility
     */
    List<Transaction> findByUserIdOrderByDateDesc(UUID userId);

    /**
     * Get top 5 transactions for a wallet
     */
    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.wallet " +
           "LEFT JOIN FETCH t.category " +
           "WHERE t.user.id = :userId AND t.wallet.id = :walletId " +
           "ORDER BY t.date DESC LIMIT 5")
    List<Transaction> findTop5ByUserIdAndWalletId(@Param("userId") UUID userId, @Param("walletId") UUID walletId);

    /**
     * Get top 5 transactions for a user
     */
    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.wallet " +
           "LEFT JOIN FETCH t.category " +
           "WHERE t.user.id = :userId " +
           "ORDER BY t.date DESC LIMIT 5")
    List<Transaction> findTop5ByUserId(@Param("userId") UUID userId);

    /**
     * Find transactions by date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.date >= :startDate AND t.date <= :endDate")
    List<Transaction> findByUserIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find transactions by wallet and date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.wallet.id = :walletId AND t.date >= :startDate AND t.date <= :endDate")
    List<Transaction> findByUserIdAndWalletIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
