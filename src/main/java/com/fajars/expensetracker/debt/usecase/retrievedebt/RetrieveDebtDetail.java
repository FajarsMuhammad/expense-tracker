package com.fajars.expensetracker.debt.usecase.retrievedebt;

import com.fajars.expensetracker.debt.api.DebtDetailResponse;

import java.util.UUID;

/**
 * Use case interface for getting detailed debt information.
 */
public interface RetrieveDebtDetail {

    /**
     * Get detailed information about a debt including payment history.
     *
     * @param debtId the ID of the debt
     * @return detailed debt response with payment history
     */
    DebtDetailResponse retrieve(UUID debtId);
}
