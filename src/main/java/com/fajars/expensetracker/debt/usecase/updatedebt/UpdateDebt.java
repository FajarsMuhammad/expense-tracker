package com.fajars.expensetracker.debt.usecase.updatedebt;

import com.fajars.expensetracker.debt.api.DebtResponse;
import com.fajars.expensetracker.debt.api.UpdateDebtRequest;

import java.util.UUID;

/**
 * Use case interface for updating an existing debt.
 */
public interface UpdateDebt {

    /**
     * Update an existing debt for a user.
     *
     * @param debtId the ID of the debt to update
     * @param request the debt update request
     * @return the updated debt response
     */
    DebtResponse update(UUID debtId, UpdateDebtRequest request);
}
