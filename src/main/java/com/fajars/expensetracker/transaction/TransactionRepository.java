package com.fajars.expensetracker.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdAndWalletIdOrderByDateDesc(UUID userId, UUID walletId);

    List<Transaction> findByUserIdOrderByDateDesc(UUID userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.wallet.id = :walletId ORDER BY t.date DESC")
    List<Transaction> findTop5ByUserIdAndWalletId(@Param("userId") UUID userId, @Param("walletId") UUID walletId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.date DESC")
    List<Transaction> findTop5ByUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.date >= :startDate AND t.date <= :endDate")
    List<Transaction> findByUserIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.wallet.id = :walletId AND t.date >= :startDate AND t.date <= :endDate")
    List<Transaction> findByUserIdAndWalletIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("walletId") UUID walletId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}
