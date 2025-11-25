package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.UpdateWalletRequest;
import com.fajars.expensetracker.wallet.WalletDto;

import java.util.UUID;

/**
 * Use Case: Update a user's wallet
 */
public interface UpdateWallet {
    WalletDto update(UUID walletId, UUID userId, UpdateWalletRequest request);
}
