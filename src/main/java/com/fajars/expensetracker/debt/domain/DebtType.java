package com.fajars.expensetracker.debt.domain;

/**
 * Represents the type of debt.
 *
 * PAYABLE - Money you owe to others (liabilities)
 * RECEIVABLE - Money owed to you (assets)
 */
public enum DebtType {
    PAYABLE,     // Money you owe
    RECEIVABLE   // Money owed to you
}
