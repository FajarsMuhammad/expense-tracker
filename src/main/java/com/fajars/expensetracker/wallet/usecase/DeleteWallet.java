package com.fajars.expensetracker.wallet.usecase;

import java.util.UUID;

/**
 * Use Case: Delete a user's wallet
 */
public interface DeleteWallet {
    void delete(UUID walletId, UUID userId);
}
