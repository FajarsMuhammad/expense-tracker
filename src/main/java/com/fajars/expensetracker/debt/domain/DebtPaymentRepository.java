package com.fajars.expensetracker.debt.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for DebtPayment entity.
 */
@Repository
public interface DebtPaymentRepository extends JpaRepository<DebtPayment, UUID> {

    /**
     * Find all payments for a specific debt.
     *
     * @param debtId the debt ID
     * @return list of payments ordered by paid date descending
     */
    @Query("SELECT dp FROM DebtPayment dp WHERE dp.debt.id = :debtId ORDER BY dp.paidAt DESC")
    List<DebtPayment> findByDebtId(@Param("debtId") UUID debtId);

    /**
     * Find all payments for a specific debt with pagination.
     *
     * @param debtId the debt ID
     * @param pageable pagination information
     * @return page of payments
     */
    @Query("SELECT dp FROM DebtPayment dp WHERE dp.debt.id = :debtId ORDER BY dp.paidAt DESC")
    Page<DebtPayment> findByDebtIdPageable(@Param("debtId") UUID debtId, Pageable pageable);

    /**
     * Calculate total paid amount for a debt.
     *
     * @param debtId the debt ID
     * @return total paid amount
     */
    @Query("SELECT COALESCE(SUM(dp.amount), 0.0) FROM DebtPayment dp WHERE dp.debt.id = :debtId")
    Double getTotalPaidAmount(@Param("debtId") UUID debtId);

    /**
     * Count payments for a debt.
     *
     * @param debtId the debt ID
     * @return count of payments
     */
    @Query("SELECT COUNT(dp) FROM DebtPayment dp WHERE dp.debt.id = :debtId")
    Long countByDebtId(@Param("debtId") UUID debtId);
}
