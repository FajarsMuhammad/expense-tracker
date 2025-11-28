package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.WalletResponse;

import java.util.UUID;

/**
 * Use Case: Find a specific wallet by ID
 */
public interface FindWalletById {
    WalletResponse findByIdAndUserId(UUID walletId, UUID userId);
}
