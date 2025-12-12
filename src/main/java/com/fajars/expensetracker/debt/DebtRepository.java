package com.fajars.expensetracker.debt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Debt entity.
 * Provides data access methods with ownership validation.
 */
@Repository
public interface DebtRepository extends JpaRepository<Debt, UUID> {

    /**
     * Find a debt by ID and ensure it belongs to the specified user.
     *
     * @param id the debt ID
     * @param userId the user ID
     * @return Optional containing the debt if found and belongs to user
     */
    @Query("SELECT d FROM Debt d WHERE d.id = :id AND d.user.id = :userId")
    Optional<Debt> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    /**
     * Find all debts for a specific user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return Page of debts
     */
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId ORDER BY d.createdAt DESC")
    Page<Debt> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find debts by user and type.
     *
     * @param userId the user ID
     * @param type the debt type
     * @param pageable pagination information
     * @return Page of debts
     */
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId AND d.type = :type ORDER BY d.createdAt DESC")
    Page<Debt> findByUserIdAndType(
        @Param("userId") UUID userId,
        @Param("type") DebtType type,
        Pageable pageable
    );

    /**
     * Find debts by user and status.
     *
     * @param userId the user ID
     * @param status the debt status
     * @param pageable pagination information
     * @return Page of debts
     */
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId AND d.status = :status ORDER BY d.createdAt DESC")
    Page<Debt> findByUserIdAndStatus(
        @Param("userId") UUID userId,
        @Param("status") DebtStatus status,
        Pageable pageable
    );

    /**
     * Find debts by user, type and status.
     *
     * @param userId the user ID
     * @param type the debt type
     * @param status the debt status
     * @param pageable pagination information
     * @return Page of debts
     */
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId AND d.type = :type AND d.status = :status ORDER BY d.createdAt DESC")
    Page<Debt> findByUserIdAndTypeAndStatus(
        @Param("userId") UUID userId,
        @Param("type") DebtType type,
        @Param("status") DebtStatus status,
        Pageable pageable
    );

    /**
     * Find overdue debts for a user.
     *
     * @param userId the user ID
     * @param currentDate the current date
     * @param pageable pagination information
     * @return Page of overdue debts
     */
    @Query("SELECT d FROM Debt d WHERE d.user.id = :userId AND d.dueDate < :currentDate AND d.status != 'PAID' ORDER BY d.dueDate ASC")
    Page<Debt> findOverdueDebts(
        @Param("userId") UUID userId,
        @Param("currentDate") LocalDateTime currentDate,
        Pageable pageable
    );

    /**
     * Get total debt amount for a user by status.
     *
     * @param userId the user ID
     * @param status the debt status
     * @return total remaining amount
     */
    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0.0) FROM Debt d WHERE d.user.id = :userId AND d.status = :status")
    Double getTotalDebtByStatus(@Param("userId") UUID userId, @Param("status") DebtStatus status);

    /**
     * Count debts by user and status.
     *
     * @param userId the user ID
     * @param status the debt status
     * @return count of debts
     */
    @Query("SELECT COUNT(d) FROM Debt d WHERE d.user.id = :userId AND d.status = :status")
    Long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") DebtStatus status);

    /**
     * Count active debts (OPEN or PARTIAL status) for a user.
     * Used for FREE tier debt limit enforcement.
     *
     * @param userId the user ID
     * @return count of active debts
     */
    @Query("SELECT COUNT(d) FROM Debt d WHERE d.user.id = :userId AND d.status IN ('OPEN', 'PARTIAL')")
    Long countActiveDebtsByUserId(@Param("userId") UUID userId);
}
