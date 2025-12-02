package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.debt.DebtDetailResponse;

import java.util.UUID;

/**
 * Use case interface for getting detailed debt information.
 */
public interface GetDebtDetail {

    /**
     * Get detailed information about a debt including payment history.
     *
     * @param userId the ID of the user owning the debt
     * @param debtId the ID of the debt
     * @return detailed debt response with payment history
     */
    DebtDetailResponse getDetail(UUID userId, UUID debtId);
}
