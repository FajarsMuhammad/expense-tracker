package com.fajars.expensetracker.debt.usecase.adddebt;

import com.fajars.expensetracker.debt.api.AddDebtPaymentRequest;
import com.fajars.expensetracker.debt.api.DebtPaymentResponse;
import com.fajars.expensetracker.debt.api.DebtResponse;

import java.util.UUID;

/**
 * Use case interface for adding a payment to a debt.
 */
public interface AddDebtPayment {

    /**
     * Add a payment to an existing debt.
     *
     * @param debtId the ID of the debt
     * @param request the payment request
     * @return the payment response with updated debt information
     */
    AddDebtPaymentResult addPayment(UUID debtId, AddDebtPaymentRequest request);

    /**
     * Result containing both the payment and updated debt information.
     */
    record AddDebtPaymentResult(
        DebtPaymentResponse payment,
        DebtResponse updatedDebt
    ) {}
}
