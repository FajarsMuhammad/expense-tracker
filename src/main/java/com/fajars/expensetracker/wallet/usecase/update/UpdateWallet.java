package com.fajars.expensetracker.wallet.usecase.update;

import com.fajars.expensetracker.wallet.api.UpdateWalletRequest;
import com.fajars.expensetracker.wallet.api.WalletResponse;

import java.util.UUID;

/**
 * Use Case: Update a user's wallet
 */
public interface UpdateWallet {
    WalletResponse update(UUID walletId, UpdateWalletRequest request);
}
