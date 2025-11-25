package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.WalletDto;

import java.util.UUID;

/**
 * Use Case: Find a specific wallet by ID
 */
public interface FindWalletById {
    WalletDto findByIdAndUserId(UUID walletId, UUID userId);
}
