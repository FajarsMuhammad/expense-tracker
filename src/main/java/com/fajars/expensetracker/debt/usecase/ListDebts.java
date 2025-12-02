package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.debt.DebtFilter;
import com.fajars.expensetracker.debt.DebtResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Use case interface for listing debts with filters.
 */
public interface ListDebts {

    /**
     * List debts for a user with optional filters.
     *
     * @param userId the ID of the user
     * @param filter filter criteria
     * @return page of debt responses
     */
    Page<DebtResponse> list(UUID userId, DebtFilter filter);
}
