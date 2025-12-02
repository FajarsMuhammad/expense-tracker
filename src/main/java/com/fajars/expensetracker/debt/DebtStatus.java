package com.fajars.expensetracker.debt;

/**
 * Represents the status of a debt.
 *
 * OPEN - Debt has no payments yet
 * PARTIAL - Debt has partial payments but not fully paid
 * PAID - Debt is fully paid
 */
public enum DebtStatus {
    OPEN,
    PARTIAL,
    PAID
}
