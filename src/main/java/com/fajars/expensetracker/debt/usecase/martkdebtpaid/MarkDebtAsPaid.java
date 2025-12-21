package com.fajars.expensetracker.debt.usecase.martkdebtpaid;

import com.fajars.expensetracker.debt.api.DebtResponse;

import java.util.UUID;

/**
 * Use case interface for marking a debt as fully paid.
 */
public interface MarkDebtAsPaid {

    /**
     * Mark a debt as fully paid, setting remaining amount to zero.
     *
     * @param debtId the ID of the debt
     * @return the updated debt response
     */
    DebtResponse markAsPaid(UUID debtId);
}
