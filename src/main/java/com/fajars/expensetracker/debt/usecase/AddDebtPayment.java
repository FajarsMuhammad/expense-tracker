package com.fajars.expensetracker.debt.usecase;

import com.fajars.expensetracker.debt.AddDebtPaymentRequest;
import com.fajars.expensetracker.debt.DebtPaymentResponse;
import com.fajars.expensetracker.debt.DebtResponse;

import java.util.UUID;

/**
 * Use case interface for adding a payment to a debt.
 */
public interface AddDebtPayment {

    /**
     * Add a payment to an existing debt.
     *
     * @param userId the ID of the user making the payment
     * @param debtId the ID of the debt
     * @param request the payment request
     * @return the payment response with updated debt information
     */
    AddDebtPaymentResult addPayment(UUID userId, UUID debtId, AddDebtPaymentRequest request);

    /**
     * Result containing both the payment and updated debt information.
     */
    record AddDebtPaymentResult(
        DebtPaymentResponse payment,
        DebtResponse updatedDebt
    ) {}
}
