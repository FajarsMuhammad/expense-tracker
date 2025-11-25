package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.transaction.Transaction;
import com.fajars.expensetracker.wallet.UpdateWalletRequest;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletDto;
import com.fajars.expensetracker.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateWalletUseCase implements UpdateWallet {

    private final WalletRepository walletRepository;
    private final BusinessEventLogger businessEventLogger;

    @Override
    @Transactional
    public WalletDto update(UUID walletId, UUID userId, UpdateWalletRequest request) {
        // Ownership check
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        // Validate name
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet name must not be empty");
        }

        // Validate initial balance
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
        String username = getCurrentUsername();
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

        return toDto(wallet);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private WalletDto toDto(Wallet wallet) {
        double currentBalance = wallet.getInitialBalance();
        if (wallet.getTransactions() != null) {
            for (Transaction transaction : wallet.getTransactions()) {
                if ("INCOME".equals(transaction.getType())) {
                    currentBalance += transaction.getAmount();
                } else if ("EXPENSE".equals(transaction.getType())) {
                    currentBalance -= transaction.getAmount();
                }
            }
        }

        return new WalletDto(
            wallet.getId(),
            wallet.getName(),
            wallet.getCurrency(),
            wallet.getInitialBalance(),
            currentBalance,
            wallet.getCreatedAt(),
            wallet.getUpdatedAt()
        );
    }
}
