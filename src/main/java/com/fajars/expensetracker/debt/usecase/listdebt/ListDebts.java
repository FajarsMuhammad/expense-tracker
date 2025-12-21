package com.fajars.expensetracker.debt.usecase.listdebt;

import com.fajars.expensetracker.debt.api.DebtFilter;
import com.fajars.expensetracker.debt.api.DebtResponse;
import org.springframework.data.domain.Page;

/**
 * Use case interface for listing debts with filters.
 */
public interface ListDebts {

    /**
     * List debts for a user with optional filters.
     *
     * @param filter filter criteria
     * @return page of debt responses
     */
    Page<DebtResponse> list(DebtFilter filter);
}
