package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.WalletResponse;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Find all wallets for a user
 */
public interface FindAllWallets {
    List<WalletResponse> findAllByUserId(UUID userId);
}
