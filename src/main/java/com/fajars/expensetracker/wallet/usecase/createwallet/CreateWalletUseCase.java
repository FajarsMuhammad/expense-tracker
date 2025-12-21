package com.fajars.expensetracker.wallet.usecase.createwallet;

import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.common.security.CurrentUserProvider;
import com.fajars.expensetracker.subscription.SubscriptionHelper;
import com.fajars.expensetracker.user.domain.User;
import com.fajars.expensetracker.wallet.api.CreateWalletRequest;
import com.fajars.expensetracker.wallet.api.WalletResponse;
import com.fajars.expensetracker.wallet.domain.Currency;
import com.fajars.expensetracker.wallet.domain.Wallet;
import com.fajars.expensetracker.wallet.domain.WalletRepository;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateWalletUseCase implements CreateWallet {

    private final WalletRepository walletRepository;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final SubscriptionHelper subscriptionHelper;
    private final CurrentUserProvider currentUserProvider;

    @Value("${app.wallet.default-name}")
    private String defaultWalletName;

    @Value("${app.wallet.default-currency}")
    private String defaultCurrencyCode;

    private static final int FREE_USER_WALLET_LIMIT = 1;

    @Override
    @Transactional
    public WalletResponse create(UUID userId, CreateWalletRequest request) {
        // Validate wallet limit for FREE users (PREMIUM users bypass this check)
        if (!subscriptionHelper.isPremiumUser(userId)) {
            long walletCount = walletRepository.countByUserId(userId);
            if (walletCount >= FREE_USER_WALLET_LIMIT) {
                throw new IllegalStateException("Free users can only create " + FREE_USER_WALLET_LIMIT + " wallet. Upgrade to premium for unlimited wallets.");
            }
        }

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet name must not be empty");
        }

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
        String username = currentUserProvider.getEmail();
        businessEventLogger.logWalletCreated(wallet.getId().getMostSignificantBits(), username, wallet.getName());
        metricsService.recordWalletCreated();

        return WalletResponse.from(wallet);
    }

    /**
     * Create default wallet for new user during registration.
     * This method bypasses the FREE tier wallet limit check since it's for brand new users.
     *
     * @param userId User ID
     * @return Created wallet entity
     */
    @Transactional
    public Wallet createDefaultForNewUser(UUID userId) {
        log.info("Creating default wallet for new user: {}", userId);

        Currency currency;
        try {
            currency = Currency.valueOf(defaultCurrencyCode);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid default currency '{}', falling back to IDR", defaultCurrencyCode);
            currency = Currency.IDR;
        }

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .name(defaultWalletName)
                .currency(currency)
                .initialBalance(0.0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        wallet = walletRepository.save(wallet);

        // Log business event and metrics
        businessEventLogger.logWalletCreated(wallet.getId().getMostSignificantBits(), "registration", wallet.getName());
        metricsService.recordWalletCreated();

        log.info("Default wallet created for user {}: walletId={}, name={}, currency={}",
                userId, wallet.getId(), wallet.getName(), wallet.getCurrency());

        return wallet;
    }
}
