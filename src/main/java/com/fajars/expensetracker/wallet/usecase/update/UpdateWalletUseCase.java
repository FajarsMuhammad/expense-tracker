package com.fajars.expensetracker.wallet.usecase.update;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.wallet.api.UpdateWalletRequest;
import com.fajars.expensetracker.wallet.api.WalletResponse;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateWalletUseCase implements UpdateWallet {

    private final WalletRepository walletRepository;
    private final BusinessEventLogger businessEventLogger;
    private final CurrentUserProvider userProvider;

    @Override
    @Transactional
    public WalletResponse update(UUID walletId, UpdateWalletRequest request) {
        UUID userId = userProvider.getUserId();

        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet name must not be empty");
        }

        if (request.initialBalance() < 0) {
            throw new IllegalArgumentException(
                "Initial balance must be greater than or equal to 0");
        }

        // Track changes for logging
        String oldName = wallet.getName();
        Double oldBalance = wallet.getInitialBalance();

        wallet.setName(request.name().trim());
        wallet.setCurrency(request.currency());
        wallet.setInitialBalance(request.initialBalance());
        wallet.setUpdatedAt(new Date());

        wallet = walletRepository.save(wallet);

        // Log business events
        String username = userProvider.getEmail();
        if (!oldName.equals(wallet.getName())) {
            businessEventLogger.logWalletUpdated(
                wallet.getId().getMostSignificantBits(), username, "name", oldName, wallet.getName()
            );
        }
        if (!oldBalance.equals(wallet.getInitialBalance())) {
            businessEventLogger.logWalletUpdated(
                wallet.getId().getMostSignificantBits(), username, "initialBalance", oldBalance,
                wallet.getInitialBalance()
            );
        }

        return WalletResponse.from(wallet);
    }
}
