package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.CreateWalletRequest;
import com.fajars.expensetracker.wallet.WalletResponse;

import java.util.UUID;

/**
 * Use Case: Create a new wallet for a user
 */
public interface CreateWallet {
    WalletResponse create(UUID userId, CreateWalletRequest request);
}
