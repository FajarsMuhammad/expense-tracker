package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.wallet.WalletDto;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Find all wallets for a user
 */
public interface FindAllWallets {
    List<WalletDto> findAllByUserId(UUID userId);
}
