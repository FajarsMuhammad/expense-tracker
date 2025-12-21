package com.fajars.expensetracker.wallet.usecase.deletewallet;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteWalletUseCase implements DeleteWallet {

    private final WalletRepository walletRepository;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider userProvider;

    @Override
    @Transactional
    public void delete(UUID walletId) {
        // Ownership check
        UUID userId = userProvider.getUserId();
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        walletRepository.delete(wallet);

        // Log business event
        String username = userProvider.getEmail();
        businessEventLogger.logWalletDeleted(wallet.getId().getMostSignificantBits(), username);
    }
}
