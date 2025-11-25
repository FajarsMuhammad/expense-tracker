package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.CreateWalletRequest;
import com.fajars.expensetracker.wallet.WalletDto;

import java.util.UUID;

/**
 * Use Case: Create a new wallet for a user
 */
public interface CreateWallet {
    WalletDto create(UUID userId, CreateWalletRequest request);
}
