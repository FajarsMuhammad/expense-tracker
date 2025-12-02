package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.debt.CreateDebtRequest;
import com.fajars.expensetracker.debt.DebtResponse;

import java.util.UUID;

/**
 * Use case interface for creating a new debt.
 */
public interface CreateDebt {

    /**
     * Create a new debt for a user.
     *
     * @param userId the ID of the user creating the debt
     * @param request the debt creation request
     * @return the created debt response
     */
    DebtResponse create(UUID userId, CreateDebtRequest request);
}
