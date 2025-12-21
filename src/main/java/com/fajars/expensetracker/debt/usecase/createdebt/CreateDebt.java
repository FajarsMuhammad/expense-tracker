package com.fajars.expensetracker.debt.usecase.createdebt;

import com.fajars.expensetracker.debt.api.CreateDebtRequest;
import com.fajars.expensetracker.debt.api.DebtResponse;

/**
 * Use case interface for creating a new debt.
 */
public interface CreateDebt {

    /**
     * Create a new debt for a user.
     *
     * @param request the debt creation request
     * @return the created debt response
     */
    DebtResponse create(CreateDebtRequest request);
}
