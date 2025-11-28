package com.fajars.expensetracker.wallet.usecase;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.wallet.CreateWalletRequest;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.WalletResponse;
import com.fajars.expensetracker.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCase implements CreateWallet {

    private final WalletRepository walletRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private static final int FREE_USER_WALLET_LIMIT = 1;

    @Override
    @Transactional
    public WalletResponse create(UUID userId, CreateWalletRequest request) {
        // Validate wallet limit for free users
        long walletCount = walletRepository.countByUserId(userId);
        if (walletCount >= FREE_USER_WALLET_LIMIT) {
            throw new IllegalStateException("Free users can only create " + FREE_USER_WALLET_LIMIT + " wallet. Upgrade to premium for unlimited wallets.");
        }

        // Validate name
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet name must not be empty");
        }

        // Validate initial balance
        if (request.initialBalance() < 0) {
            throw new IllegalArgumentException("Initial balance must be greater than or equal to 0");
        }

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .name(request.name().trim())
                .currency(request.currency())
                .initialBalance(request.initialBalance())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        wallet = walletRepository.save(wallet);

        // Log business event and metrics
        String username = getCurrentUsername();
        businessEventLogger.logWalletCreated(wallet.getId().getMostSignificantBits(), username, wallet.getName());
        metricsService.recordWalletCreated();

        return WalletResponse.from(wallet);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
