package com.fajars.expensetracker.wallet.usecase.createwallet;

import com.fajars.expensetracker.wallet.api.CreateWalletRequest;
import com.fajars.expensetracker.wallet.api.WalletResponse;

import java.util.UUID;

/**
 * Use Case: Create a new wallet for a user
 */
public interface CreateWallet {
    WalletResponse create(UUID userId, CreateWalletRequest request);
}
