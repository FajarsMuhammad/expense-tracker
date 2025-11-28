package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.UpdateWalletRequest;
import com.fajars.expensetracker.wallet.WalletResponse;

import java.util.UUID;

/**
 * Use Case: Update a user's wallet
 */
public interface UpdateWallet {
    WalletResponse update(UUID walletId, UUID userId, UpdateWalletRequest request);
}
