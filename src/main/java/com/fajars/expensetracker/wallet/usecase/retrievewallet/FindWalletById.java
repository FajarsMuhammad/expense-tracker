package com.fajars.expensetracker.wallet.usecase.retrievewallet;

import com.fajars.expensetracker.wallet.api.WalletResponse;

import java.util.UUID;

/**
 * Use Case: Find a specific wallet by ID
 */
public interface FindWalletById {
    WalletResponse findByIdAndUserId(UUID walletId, UUID userId);
}
