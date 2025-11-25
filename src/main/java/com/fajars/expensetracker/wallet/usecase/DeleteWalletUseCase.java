package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteWalletUseCase implements DeleteWallet {

    private final WalletRepository walletRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public void delete(UUID walletId, UUID userId) {
        // Ownership check
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        walletRepository.delete(wallet);

        // Log business event
        String username = getCurrentUsername();
        businessEventLogger.logWalletDeleted(wallet.getId().getMostSignificantBits(), username);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
