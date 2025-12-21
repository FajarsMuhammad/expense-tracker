package com.fajars.expensetracker.wallet.usecase.deletewallet;

import java.util.UUID;

/**
 * Use Case: Delete a user's wallet
 */
public interface DeleteWallet {
    void delete(UUID walletId);
}
