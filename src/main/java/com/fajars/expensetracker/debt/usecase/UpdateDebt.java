package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.debt.DebtResponse;
import com.fajars.expensetracker.debt.UpdateDebtRequest;

import java.util.UUID;

/**
 * Use case interface for updating an existing debt.
 */
public interface UpdateDebt {

    /**
     * Update an existing debt for a user.
     *
     * @param userId the ID of the user owning the debt
     * @param debtId the ID of the debt to update
     * @param request the debt update request
     * @return the updated debt response
     */
    DebtResponse update(UUID userId, UUID debtId, UpdateDebtRequest request);
}
