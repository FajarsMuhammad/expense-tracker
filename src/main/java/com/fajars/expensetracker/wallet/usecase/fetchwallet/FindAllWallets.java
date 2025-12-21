package com.fajars.expensetracker.wallet.usecase.fetchwallet;

import com.fajars.expensetracker.wallet.api.WalletResponse;
import java.util.List;

/**
 * Use Case: Find all wallets for a user
 */
public interface FindAllWallets {
    List<WalletResponse> findAllByUserId();
}
